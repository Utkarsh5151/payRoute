package com.payroute.platform.provider.dto;

import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.provider.enums.ProviderStatus;
import java.math.BigDecimal;
import java.util.UUID;

public class ProviderResponseDto {

    private UUID id;
    private String code;
    private String name;
    private ProviderStatus status;
    private BigDecimal successRate;
    private Integer avgLatencyMs;
    private BigDecimal timeoutRate;
    private BigDecimal failureRate;
    private Integer priorityWeight;

    public ProviderResponseDto() {
    }

    public static ProviderResponseDto from(PaymentProvider provider) {
        ProviderResponseDto dto = new ProviderResponseDto();
        dto.setId(provider.getId());
        dto.setCode(provider.getCode());
        dto.setName(provider.getName());
        dto.setStatus(provider.getStatus());
        dto.setSuccessRate(provider.getSuccessRate());
        dto.setAvgLatencyMs(provider.getAvgLatencyMs());
        dto.setTimeoutRate(provider.getTimeoutRate());
        dto.setFailureRate(provider.getFailureRate());
        dto.setPriorityWeight(provider.getPriorityWeight());
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProviderStatus getStatus() {
        return status;
    }

    public void setStatus(ProviderStatus status) {
        this.status = status;
    }

    public BigDecimal getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(BigDecimal successRate) {
        this.successRate = successRate;
    }

    public Integer getAvgLatencyMs() {
        return avgLatencyMs;
    }

    public void setAvgLatencyMs(Integer avgLatencyMs) {
        this.avgLatencyMs = avgLatencyMs;
    }

    public BigDecimal getTimeoutRate() {
        return timeoutRate;
    }

    public void setTimeoutRate(BigDecimal timeoutRate) {
        this.timeoutRate = timeoutRate;
    }

    public BigDecimal getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(BigDecimal failureRate) {
        this.failureRate = failureRate;
    }

    public Integer getPriorityWeight() {
        return priorityWeight;
    }

    public void setPriorityWeight(Integer priorityWeight) {
        this.priorityWeight = priorityWeight;
    }
}
