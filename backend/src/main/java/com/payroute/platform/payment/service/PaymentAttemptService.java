package com.payroute.platform.payment.service;

import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.entity.PaymentAttempt;
import com.payroute.platform.payment.repository.PaymentAttemptRepository;
import com.payroute.platform.provider.client.PaymentProviderClient;
import com.payroute.platform.provider.client.ProviderResult;
import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.provider.enums.AttemptStatus;
import com.payroute.platform.provider.service.ProviderConfigService;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentAttemptService {

    private final PaymentAttemptRepository attemptRepository;
    private final ProviderConfigService providerConfigService;

    public PaymentAttemptService(PaymentAttemptRepository attemptRepository,
                                 ProviderConfigService providerConfigService) {
        this.attemptRepository = attemptRepository;
        this.providerConfigService = providerConfigService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProviderResult executeAndRecordAttempt(Payment payment, PaymentProvider provider, int attemptNumber) {
        PaymentProviderClient client = providerConfigService.getClient(provider.getCode());

        Instant start = Instant.now();
        ProviderResult result = client.executePayment(payment, provider);
        Instant completed = Instant.now();

        PaymentAttempt attempt = new PaymentAttempt(
                payment,
                provider,
                attemptNumber,
                result.getStatus(),
                result.getRequestSnapshot(),
                result.getResponseSnapshot(),
                result.getErrorCode(),
                result.getErrorMessage(),
                result.getLatencyMs(),
                start,
                completed
        );

        attemptRepository.saveAndFlush(attempt);
        return result;
    }
}
