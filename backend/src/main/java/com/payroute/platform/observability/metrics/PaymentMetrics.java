package com.payroute.platform.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetrics {

    private final MeterRegistry meterRegistry;

    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordPaymentProcessed(String status, String paymentMethod, String provider) {
        Counter.builder("payment.processed.total")
                .description("Total number of processed payments")
                .tag("status", status != null ? status : "UNKNOWN")
                .tag("method", paymentMethod != null ? paymentMethod : "UNKNOWN")
                .tag("provider", provider != null ? provider : "NONE")
                .register(meterRegistry)
                .increment();
    }

    public void recordProviderAttempt(String providerCode, String status) {
        Counter.builder("provider.attempt.counter")
                .description("Total number of provider invocation attempts")
                .tag("provider", providerCode)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentLatency(long durationMs) {
        Timer.builder("payment.latency.timer")
                .description("End-to-end payment processing latency")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
