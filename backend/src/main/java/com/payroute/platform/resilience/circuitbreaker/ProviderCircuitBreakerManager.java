package com.payroute.platform.resilience.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class ProviderCircuitBreakerManager {

    private final CircuitBreakerRegistry registry;

    public ProviderCircuitBreakerManager(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    public String getCircuitBreakerState(String providerCode) {
        CircuitBreaker cb = registry.circuitBreaker(providerCode);
        return cb.getState().name();
    }

    public Map<String, String> getAllCircuitBreakerStates() {
        Map<String, String> states = new HashMap<>();
        for (CircuitBreaker cb : registry.getAllCircuitBreakers()) {
            states.put(cb.getName(), cb.getState().name());
        }
        return states;
    }

    public boolean isCallPermitted(String providerCode) {
        CircuitBreaker cb = registry.circuitBreaker(providerCode);
        return cb.tryAcquirePermission();
    }

    public void recordSuccess(String providerCode, long durationMs) {
        CircuitBreaker cb = registry.circuitBreaker(providerCode);
        cb.onSuccess(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordFailure(String providerCode, long durationMs, Throwable throwable) {
        CircuitBreaker cb = registry.circuitBreaker(providerCode);
        cb.onError(durationMs, TimeUnit.MILLISECONDS, throwable);
    }
}
