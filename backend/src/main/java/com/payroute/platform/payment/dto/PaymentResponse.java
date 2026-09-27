package com.payroute.platform.payment.dto;

import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.enums.PaymentMethod;
import com.payroute.platform.payment.enums.PaymentStatus;
import com.payroute.platform.provider.dto.ProviderSummaryDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentResponse {

    private UUID id;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String customerEmail;
    private String clientReferenceId;
    private String description;
    private ProviderSummaryDto selectedProvider;
    private int attemptCount;
    private Instant createdAt;
    private Instant updatedAt;

    public PaymentResponse() {
    }

    public static PaymentResponse from(Payment payment, int attemptCount) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setCustomerEmail(payment.getCustomerEmail());
        response.setClientReferenceId(payment.getClientReferenceId());
        response.setDescription(payment.getDescription());
        response.setAttemptCount(attemptCount);
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        if (payment.getSelectedProvider() != null) {
            response.setSelectedProvider(new ProviderSummaryDto(
                    payment.getSelectedProvider().getId(),
                    payment.getSelectedProvider().getCode(),
                    payment.getSelectedProvider().getName()
            ));
        }

        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getClientReferenceId() {
        return clientReferenceId;
    }

    public void setClientReferenceId(String clientReferenceId) {
        this.clientReferenceId = clientReferenceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProviderSummaryDto getSelectedProvider() {
        return selectedProvider;
    }

    public void setSelectedProvider(ProviderSummaryDto selectedProvider) {
        this.selectedProvider = selectedProvider;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
