package com.payroute.platform.payment.dto;

import com.payroute.platform.payment.entity.PaymentAttempt;
import com.payroute.platform.provider.enums.AttemptStatus;
import java.time.Instant;
import java.util.UUID;

public class PaymentAttemptDto {

    private UUID id;
    private UUID providerId;
    private String providerCode;
    private String providerName;
    private Integer attemptNumber;
    private AttemptStatus status;
    private String requestPayload;
    private String responsePayload;
    private String errorCode;
    private String errorMessage;
    private Integer latencyMs;
    private Instant startedAt;
    private Instant completedAt;

    public PaymentAttemptDto() {
    }

    public static PaymentAttemptDto from(PaymentAttempt attempt) {
        PaymentAttemptDto dto = new PaymentAttemptDto();
        dto.setId(attempt.getId());
        if (attempt.getProvider() != null) {
            dto.setProviderId(attempt.getProvider().getId());
            dto.setProviderCode(attempt.getProvider().getCode());
            dto.setProviderName(attempt.getProvider().getName());
        }
        dto.setAttemptNumber(attempt.getAttemptNumber());
        dto.setStatus(attempt.getStatus());
        dto.setRequestPayload(attempt.getRequestPayload());
        dto.setResponsePayload(attempt.getResponsePayload());
        dto.setErrorCode(attempt.getErrorCode());
        dto.setErrorMessage(attempt.getErrorMessage());
        dto.setLatencyMs(attempt.getLatencyMs());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setCompletedAt(attempt.getCompletedAt());
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
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

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
