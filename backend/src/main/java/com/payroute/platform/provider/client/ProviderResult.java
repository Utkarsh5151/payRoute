package com.payroute.platform.provider.client;

import com.payroute.platform.provider.enums.AttemptStatus;

public class ProviderResult {

    private final AttemptStatus status;
    private final String transactionId;
    private final String errorCode;
    private final String errorMessage;
    private final int latencyMs;
    private final String requestSnapshot;
    private final String responseSnapshot;

    public ProviderResult(AttemptStatus status, String transactionId, String errorCode,
                          String errorMessage, int latencyMs, String requestSnapshot, String responseSnapshot) {
        this.status = status;
        this.transactionId = transactionId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.latencyMs = latencyMs;
        this.requestSnapshot = requestSnapshot;
        this.responseSnapshot = responseSnapshot;
    }

    public static ProviderResult success(String transactionId, int latencyMs, String requestSnapshot, String responseSnapshot) {
        return new ProviderResult(AttemptStatus.SUCCESS, transactionId, null, null, latencyMs, requestSnapshot, responseSnapshot);
    }

    public static ProviderResult failure(String errorCode, String errorMessage, int latencyMs, String requestSnapshot, String responseSnapshot) {
        return new ProviderResult(AttemptStatus.FAILED, null, errorCode, errorMessage, latencyMs, requestSnapshot, responseSnapshot);
    }

    public static ProviderResult timeout(int latencyMs, String requestSnapshot, String responseSnapshot) {
        return new ProviderResult(AttemptStatus.TIMEOUT, null, "PROVIDER_TIMEOUT", "Provider read timeout occurred", latencyMs, requestSnapshot, responseSnapshot);
    }

    public static ProviderResult circuitOpen(int latencyMs, String requestSnapshot) {
        return new ProviderResult(AttemptStatus.CIRCUIT_OPEN, null, "CIRCUIT_BREAKER_OPEN", "Provider circuit breaker is open", latencyMs, requestSnapshot, "{\"status\":\"CIRCUIT_OPEN\"}");
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == AttemptStatus.SUCCESS;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public String getRequestSnapshot() {
        return requestSnapshot;
    }

    public String getResponseSnapshot() {
        return responseSnapshot;
    }
}
