package com.payroute.platform.payment.entity;

import com.payroute.platform.common.entity.BaseAuditEntity;
import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.payment.enums.PaymentMethod;
import com.payroute.platform.payment.enums.PaymentStatus;
import com.payroute.platform.provider.entity.PaymentProvider;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 32)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "client_reference_id", length = 128)
    private String clientReferenceId;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_provider_id")
    private PaymentProvider selectedProvider;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public Payment() {
    }

    public Payment(Merchant merchant, UUID userId, BigDecimal amount, String currency,
                   PaymentMethod paymentMethod, String customerEmail, String description,
                   String clientReferenceId, String idempotencyKey) {
        this.merchant = merchant;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency != null ? currency.toUpperCase() : "USD";
        this.paymentMethod = paymentMethod;
        this.customerEmail = customerEmail;
        this.description = description;
        this.clientReferenceId = clientReferenceId;
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.CREATED;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getClientReferenceId() {
        return clientReferenceId;
    }

    public void setClientReferenceId(String clientReferenceId) {
        this.clientReferenceId = clientReferenceId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PaymentProvider getSelectedProvider() {
        return selectedProvider;
    }

    public void setSelectedProvider(PaymentProvider selectedProvider) {
        this.selectedProvider = selectedProvider;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
