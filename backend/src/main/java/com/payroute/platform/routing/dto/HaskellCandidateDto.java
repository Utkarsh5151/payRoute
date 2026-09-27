package com.payroute.platform.routing.dto;

public class HaskellCandidateDto {

    private String providerId;
    private String providerCode;
    private String providerName;
    private double successRate;
    private double latencyMs;
    private double timeoutRate;
    private double failureRate;
    private String circuitBreakerState; // "CLOSED", "HALF_OPEN", "OPEN"
    private int priorityWeight;

    public HaskellCandidateDto() {
    }

    public HaskellCandidateDto(String providerId, String providerCode, String providerName,
                               double successRate, double latencyMs, double timeoutRate,
                               double failureRate, String circuitBreakerState, int priorityWeight) {
        this.providerId = providerId;
        this.providerCode = providerCode;
        this.providerName = providerName;
        this.successRate = successRate;
        this.latencyMs = latencyMs;
        this.timeoutRate = timeoutRate;
        this.failureRate = failureRate;
        this.circuitBreakerState = circuitBreakerState;
        this.priorityWeight = priorityWeight;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(double latencyMs) {
        this.latencyMs = latencyMs;
    }

    public double getTimeoutRate() {
        return timeoutRate;
    }

    public void setTimeoutRate(double timeoutRate) {
        this.timeoutRate = timeoutRate;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public String getCircuitBreakerState() {
        return circuitBreakerState;
    }

    public void setCircuitBreakerState(String circuitBreakerState) {
        this.circuitBreakerState = circuitBreakerState;
    }

    public int getPriorityWeight() {
        return priorityWeight;
    }

    public void setPriorityWeight(int priorityWeight) {
        this.priorityWeight = priorityWeight;
    }
}
