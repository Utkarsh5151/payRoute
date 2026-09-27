package com.payroute.platform.observability.dto;

import com.payroute.platform.payment.dto.PaymentResponse;
import com.payroute.platform.provider.dto.ProviderResponseDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardStatsDto {

    private long totalPayments;
    private long successfulPayments;
    private long failedPayments;
    private long pendingPayments;
    private BigDecimal totalVolume;
    private double successRatePercentage;
    private List<ProviderStatusCard> providerCards;
    private List<PaymentResponse> recentPayments;

    public DashboardStatsDto() {
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public long getSuccessfulPayments() {
        return successfulPayments;
    }

    public void setSuccessfulPayments(long successfulPayments) {
        this.successfulPayments = successfulPayments;
    }

    public long getFailedPayments() {
        return failedPayments;
    }

    public void setFailedPayments(long failedPayments) {
        this.failedPayments = failedPayments;
    }

    public long getPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(long pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }

    public double getSuccessRatePercentage() {
        return successRatePercentage;
    }

    public void setSuccessRatePercentage(double successRatePercentage) {
        this.successRatePercentage = successRatePercentage;
    }

    public List<ProviderStatusCard> getProviderCards() {
        return providerCards;
    }

    public void setProviderCards(List<ProviderStatusCard> providerCards) {
        this.providerCards = providerCards;
    }

    public List<PaymentResponse> getRecentPayments() {
        return recentPayments;
    }

    public void setRecentPayments(List<PaymentResponse> recentPayments) {
        this.recentPayments = recentPayments;
    }

    public static class ProviderStatusCard {
        private String code;
        private String name;
        private String status;
        private double successRate;
        private int avgLatencyMs;
        private String circuitBreakerState;

        public ProviderStatusCard() {
        }

        public ProviderStatusCard(String code, String name, String status, double successRate, int avgLatencyMs, String circuitBreakerState) {
            this.code = code;
            this.name = name;
            this.status = status;
            this.successRate = successRate;
            this.avgLatencyMs = avgLatencyMs;
            this.circuitBreakerState = circuitBreakerState;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }

        public int getAvgLatencyMs() {
            return avgLatencyMs;
        }

        public void setAvgLatencyMs(int avgLatencyMs) {
            this.avgLatencyMs = avgLatencyMs;
        }

        public String getCircuitBreakerState() {
            return circuitBreakerState;
        }

        public void setCircuitBreakerState(String circuitBreakerState) {
            this.circuitBreakerState = circuitBreakerState;
        }
    }
}
