package com.payroute.platform.payment.dto;

import com.payroute.platform.payment.entity.Refund;
import com.payroute.platform.payment.enums.RefundStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class RefundResponse {

    private UUID refundId;
    private UUID paymentId;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private RefundStatus status;
    private Instant createdAt;

    public RefundResponse() {
    }

    public static RefundResponse from(Refund refund) {
        RefundResponse response = new RefundResponse();
        response.setRefundId(refund.getId());
        response.setPaymentId(refund.getPayment().getId());
        response.setAmount(refund.getAmount());
        response.setCurrency(refund.getCurrency());
        response.setReason(refund.getReason());
        response.setStatus(refund.getStatus());
        response.setCreatedAt(refund.getCreatedAt());
        return response;
    }

    public UUID getRefundId() {
        return refundId;
    }

    public void setRefundId(UUID refundId) {
        this.refundId = refundId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
