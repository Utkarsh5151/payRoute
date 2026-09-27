package com.payroute.platform.provider.mock;

import com.payroute.platform.common.util.JsonUtils;
import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.provider.client.PaymentProviderClient;
import com.payroute.platform.provider.client.ProviderResult;
import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.provider.enums.AttemptStatus;
import com.payroute.platform.provider.enums.ProviderStatus;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMockProviderClient implements PaymentProviderClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Random random = new Random();

    @Override
    public ProviderResult executePayment(Payment payment, PaymentProvider config) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> requestMap = Map.of(
                "provider", getProviderCode(),
                "paymentId", payment.getId().toString(),
                "amount", payment.getAmount(),
                "currency", payment.getCurrency(),
                "paymentMethod", payment.getPaymentMethod().name(),
                "customerEmail", payment.getCustomerEmail()
        );
        String requestSnapshot = JsonUtils.toJson(requestMap);

        if (config.getStatus() == ProviderStatus.INACTIVE) {
            int elapsed = (int) (System.currentTimeMillis() - startTime);
            return ProviderResult.failure("PROVIDER_INACTIVE", "Provider " + getProviderCode() + " is currently INACTIVE",
                    elapsed, requestSnapshot, "{\"error\":\"PROVIDER_INACTIVE\"}");
        }

        // Simulate network latency (base +/- 20%)
        int baseLatency = config.getAvgLatencyMs() != null ? config.getAvgLatencyMs() : 200;
        int jitter = (int) (baseLatency * 0.2);
        int simulatedDelay = Math.max(10, baseLatency + (random.nextInt(Math.max(1, jitter * 2 + 1)) - jitter));

        try {
            // Keep sleep reasonable for tests/execution (capped at simulatedDelay, or lower if needed)
            Thread.sleep(Math.min(simulatedDelay, 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int elapsed = (int) (System.currentTimeMillis() - startTime);

        // Calculate probabilistic outcome based on percentages (0-100)
        double roll = random.nextDouble() * 100.0;
        double timeoutCutoff = config.getTimeoutRate() != null ? config.getTimeoutRate().doubleValue() : 0.0;
        double failureCutoff = timeoutCutoff + (config.getFailureRate() != null ? config.getFailureRate().doubleValue() : 0.0);

        if (roll < timeoutCutoff) {
            log.warn("Mock provider {} simulated TIMEOUT for payment {}", getProviderCode(), payment.getId());
            return ProviderResult.timeout(elapsed, requestSnapshot, "{\"error\":\"TIMEOUT\"}");
        } else if (roll < failureCutoff) {
            String[] errors = {"DECLINED_INSUFFICIENT_FUNDS", "DECLINED_CARD_EXPIRED", "DECLINED_RISK_CHECK"};
            String chosenError = errors[random.nextInt(errors.length)];
            log.warn("Mock provider {} simulated FAILURE [{}] for payment {}", getProviderCode(), chosenError, payment.getId());
            return ProviderResult.failure(chosenError, "Transaction declined by issuer: " + chosenError,
                    elapsed, requestSnapshot, "{\"status\":\"DECLINED\",\"errorCode\":\"" + chosenError + "\"}");
        } else {
            String transactionId = "TXN-" + getProviderCode() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("Mock provider {} processed payment {} successfully (txn: {})", getProviderCode(), payment.getId(), transactionId);
            Map<String, Object> resp = Map.of(
                    "status", "APPROVED",
                    "transactionId", transactionId,
                    "authCode", "AUTH" + (100000 + random.nextInt(900000))
            );
            return ProviderResult.success(transactionId, elapsed, requestSnapshot, JsonUtils.toJson(resp));
        }
    }
}
