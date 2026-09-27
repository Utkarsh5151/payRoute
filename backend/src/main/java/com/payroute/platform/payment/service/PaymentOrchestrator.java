package com.payroute.platform.payment.service;

import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.common.util.JsonUtils;
import com.payroute.platform.kafka.producer.PaymentEventProducer;
import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.observability.metrics.PaymentMetrics;
import com.payroute.platform.payment.dto.CreatePaymentRequest;
import com.payroute.platform.payment.dto.PaymentResponse;
import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.entity.PaymentAttempt;
import com.payroute.platform.payment.enums.PaymentStatus;
import com.payroute.platform.payment.idempotency.IdempotencyRecord;
import com.payroute.platform.payment.idempotency.IdempotencyService;
import com.payroute.platform.payment.repository.PaymentAttemptRepository;
import com.payroute.platform.payment.repository.PaymentRepository;
import com.payroute.platform.payment.statemachine.PaymentStateMachine;
import com.payroute.platform.provider.client.ProviderResult;
import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.provider.enums.AttemptStatus;
import com.payroute.platform.resilience.circuitbreaker.ProviderCircuitBreakerManager;
import com.payroute.platform.routing.dto.RoutingDecision;
import com.payroute.platform.routing.service.RoutingService;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PaymentOrchestrator.class);

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository attemptRepository;
    private final PaymentStateMachine stateMachine;
    private final PaymentAttemptService attemptService;
    private final RoutingService routingService;
    private final IdempotencyService idempotencyService;
    private final ProviderCircuitBreakerManager circuitBreakerManager;
    private final PaymentMetrics paymentMetrics;
    private final PaymentEventProducer eventProducer;

    public PaymentOrchestrator(PaymentRepository paymentRepository,
                               PaymentAttemptRepository attemptRepository,
                               PaymentStateMachine stateMachine,
                               PaymentAttemptService attemptService,
                               RoutingService routingService,
                               IdempotencyService idempotencyService,
                               ProviderCircuitBreakerManager circuitBreakerManager,
                               PaymentMetrics paymentMetrics,
                               PaymentEventProducer eventProducer) {
        this.paymentRepository = paymentRepository;
        this.attemptRepository = attemptRepository;
        this.stateMachine = stateMachine;
        this.attemptService = attemptService;
        this.routingService = routingService;
        this.idempotencyService = idempotencyService;
        this.circuitBreakerManager = circuitBreakerManager;
        this.paymentMetrics = paymentMetrics;
        this.eventProducer = eventProducer;
    }

    public PaymentResponse processPayment(CreatePaymentRequest request, Merchant merchant,
                                          UUID userId, String idempotencyKey) {
        long overallStart = System.currentTimeMillis();

        // 1. Idempotency verification and distributed lock acquisition
        Optional<IdempotencyRecord> replayRecord = idempotencyService.checkOrLock(
                idempotencyKey, merchant, "/api/payments", request);

        if (replayRecord.isPresent()) {
            IdempotencyRecord cached = replayRecord.get();
            log.info("Replaying cached idempotent response for key: {}", idempotencyKey);
            return JsonUtils.fromJson(cached.getResponseBody(), PaymentResponse.class);
        }

        Payment payment = new Payment(
                merchant,
                userId,
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentMethod(),
                request.getCustomerEmail(),
                request.getDescription(),
                request.getClientReferenceId(),
                idempotencyKey
        );

        // 2. Initial insert in CREATED state
        payment = paymentRepository.saveAndFlush(payment);

        try {
            // 3. Transition CREATED -> PROCESSING
            payment = stateMachine.transition(payment, PaymentStatus.PROCESSING, "Acquiring provider routing lock");

            // 4. Evaluate circuit breakers and obtain intelligent routing decision
            Map<String, String> cbStates = circuitBreakerManager.getAllCircuitBreakerStates();
            RoutingDecision decision = routingService.routePayment(payment, cbStates);

            List<PaymentProvider> fallbackChain = decision.getRankedProviders();
            log.info("Executing payment {} routing plan using [{}]. Primary: {}, Chain size: {}",
                    payment.getId(), decision.getEngineSource(), decision.getSelectedProvider().getCode(), fallbackChain.size());

            boolean success = false;
            int attemptSequence = 1;

            // 5. Orchestrate provider execution across fallback chain
            for (PaymentProvider provider : fallbackChain) {
                String code = provider.getCode();

                if (!circuitBreakerManager.isCallPermitted(code)) {
                    log.warn("Skipping provider {} for payment {}: Circuit breaker is OPEN", code, payment.getId());
                    PaymentAttempt cbAttempt = new PaymentAttempt(
                            payment, provider, attemptSequence++, AttemptStatus.CIRCUIT_OPEN,
                            "{}", "{\"error\":\"CIRCUIT_BREAKER_OPEN\"}", "CIRCUIT_BREAKER_OPEN",
                            "Circuit breaker rejected call", 0, Instant.now(), Instant.now()
                    );
                    attemptRepository.saveAndFlush(cbAttempt);
                    paymentMetrics.recordProviderAttempt(code, "CIRCUIT_OPEN");
                    continue;
                }

                ProviderResult result = attemptService.executeAndRecordAttempt(payment, provider, attemptSequence++);
                paymentMetrics.recordProviderAttempt(code, result.getStatus().name());

                if (result.isSuccess()) {
                    circuitBreakerManager.recordSuccess(code, result.getLatencyMs());
                    payment.setSelectedProvider(provider);
                    payment = stateMachine.transition(payment, PaymentStatus.SUCCESS,
                            "Payment approved by " + code + " (txn: " + result.getTransactionId() + ")");
                    success = true;
                    break;
                } else {
                    circuitBreakerManager.recordFailure(code, result.getLatencyMs(),
                            new RuntimeException(result.getErrorMessage()));
                    log.warn("Attempt with provider {} failed with error {}. Trying next fallback provider if available...",
                            code, result.getErrorCode());
                }
            }

            // 6. Handle terminal state
            if (!success) {
                payment = stateMachine.transition(payment, PaymentStatus.FAILED, "All eligible provider attempts failed or timed out");
                paymentMetrics.recordPaymentProcessed("FAILED", payment.getPaymentMethod().name(), "NONE");
                idempotencyService.recordFailure(idempotencyKey, merchant.getId());

                eventProducer.publishEvent(payment.getId().toString(), "PAYMENT_FAILED", Map.of(
                        "paymentId", payment.getId().toString(),
                        "merchantId", merchant.getId().toString(),
                        "amount", payment.getAmount(),
                        "currency", payment.getCurrency()
                ));
            } else {
                paymentMetrics.recordPaymentProcessed("SUCCESS", payment.getPaymentMethod().name(), payment.getSelectedProvider().getCode());
                eventProducer.publishEvent(payment.getId().toString(), "PAYMENT_SUCCEEDED", Map.of(
                        "paymentId", payment.getId().toString(),
                        "merchantId", merchant.getId().toString(),
                        "amount", payment.getAmount(),
                        "currency", payment.getCurrency(),
                        "provider", payment.getSelectedProvider().getCode()
                ));
            }

            long totalDuration = System.currentTimeMillis() - overallStart;
            paymentMetrics.recordPaymentLatency(totalDuration);

            int totalAttempts = (int) attemptRepository.countByPaymentId(payment.getId());
            PaymentResponse response = PaymentResponse.from(payment, totalAttempts);

            // 7. Record idempotency completion
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyService.recordSuccess(idempotencyKey, merchant.getId(), 201, response);
            }

            return response;

        } catch (Exception ex) {
            log.error("Fatal exception in payment orchestrator for payment {}: {}", payment.getId(), ex.getMessage(), ex);
            idempotencyService.recordFailure(idempotencyKey, merchant.getId());
            throw ex;
        }
    }
}
