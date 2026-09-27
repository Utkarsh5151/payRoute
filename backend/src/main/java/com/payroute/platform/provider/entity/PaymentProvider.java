package com.payroute.platform.provider.entity;

import com.payroute.platform.common.entity.BaseAuditEntity;
import com.payroute.platform.provider.enums.ProviderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment_providers")
public class PaymentProvider extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProviderStatus status = ProviderStatus.ACTIVE;

    @Column(name = "success_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal successRate = new BigDecimal("98.00");

    @Column(name = "avg_latency_ms", nullable = false)
    private Integer avgLatencyMs = 250;

    @Column(name = "timeout_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal timeoutRate = new BigDecimal("2.00");

    @Column(name = "failure_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal failureRate = new BigDecimal("2.00");

    @Column(name = "priority_weight", nullable = false)
    private Integer priorityWeight = 100;

    public PaymentProvider() {
    }

    public PaymentProvider(String code, String name, ProviderStatus status, BigDecimal successRate,
                           Integer avgLatencyMs, BigDecimal timeoutRate, BigDecimal failureRate, Integer priorityWeight) {
        this.code = code;
        this.name = name;
        this.status = status;
        this.successRate = successRate;
        this.avgLatencyMs = avgLatencyMs;
        this.timeoutRate = timeoutRate;
        this.failureRate = failureRate;
        this.priorityWeight = priorityWeight;
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
