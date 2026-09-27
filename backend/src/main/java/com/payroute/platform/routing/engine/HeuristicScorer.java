package com.payroute.platform.routing.engine;

import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.enums.PaymentMethod;
import com.payroute.platform.provider.entity.PaymentProvider;
import org.springframework.stereotype.Component;

@Component
public class HeuristicScorer {

    public double calculateLatencySubscore(double latencyMs) {
        if (latencyMs <= 100.0) {
            return 100.0;
        }
        if (latencyMs >= 2000.0) {
            return 0.0;
        }
        return Math.max(0.0, 100.0 - ((latencyMs - 100.0) / 19.0));
    }

    public double calculateMethodAffinity(PaymentMethod method, String providerCode) {
        if (method == PaymentMethod.UPI && providerCode != null && providerCode.startsWith("PROVIDER_A")) {
            return 1.05;
        }
        if (method == PaymentMethod.CARD && providerCode != null && providerCode.startsWith("PROVIDER_B")) {
            return 1.05;
        }
        return 1.00;
    }

    public double scoreProvider(Payment payment, PaymentProvider provider, String circuitBreakerState) {
        double cbMult = 1.0;
        if ("HALF_OPEN".equalsIgnoreCase(circuitBreakerState)) {
            cbMult = 0.4;
        } else if ("OPEN".equalsIgnoreCase(circuitBreakerState)) {
            cbMult = 0.0;
        }

        double latencyMs = provider.getAvgLatencyMs() != null ? provider.getAvgLatencyMs() : 250.0;
        double latScore = calculateLatencySubscore(latencyMs);

        double succScore = provider.getSuccessRate() != null ?
                Math.max(0.0, Math.min(100.0, provider.getSuccessRate().doubleValue())) : 95.0;

        double prioScore = provider.getPriorityWeight() != null ?
                Math.max(0.0, Math.min(100.0, provider.getPriorityWeight())) : 100.0;

        double timeoutRate = provider.getTimeoutRate() != null ? provider.getTimeoutRate().doubleValue() : 2.0;

        double rawScore = (succScore * 0.45) + (latScore * 0.30) + (prioScore * 0.15) + ((100.0 - timeoutRate) * 0.10);
        double affinity = calculateMethodAffinity(payment.getPaymentMethod(), provider.getCode());

        return rawScore * cbMult * affinity;
    }
}
