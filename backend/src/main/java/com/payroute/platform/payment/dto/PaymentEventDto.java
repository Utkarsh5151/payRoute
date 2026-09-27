package com.payroute.platform.payment.dto;

import com.payroute.platform.payment.entity.PaymentEvent;
import com.payroute.platform.payment.enums.EventType;
import com.payroute.platform.payment.enums.PaymentStatus;
import java.time.Instant;
import java.util.UUID;

public class PaymentEventDto {

    private UUID id;
    private EventType eventType;
    private PaymentStatus fromStatus;
    private PaymentStatus toStatus;
    private String payload;
    private Instant createdAt;

    public PaymentEventDto() {
    }

    public static PaymentEventDto from(PaymentEvent event) {
        PaymentEventDto dto = new PaymentEventDto();
        dto.setId(event.getId());
        dto.setEventType(event.getEventType());
        dto.setFromStatus(event.getFromStatus());
        dto.setToStatus(event.getToStatus());
        dto.setPayload(event.getPayload());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public PaymentStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(PaymentStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public PaymentStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(PaymentStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
