package com.payroute.platform.payment.enums;

import java.util.Map;
import java.util.Set;

public enum PaymentStatus {
    CREATED,
    PROCESSING,
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUND_PENDING,
    REFUNDED;

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED_TRANSITIONS = Map.of(
            CREATED, Set.of(PROCESSING, CANCELLED),
            PROCESSING, Set.of(SUCCESS, FAILED, PENDING),
            PENDING, Set.of(SUCCESS, FAILED),
            SUCCESS, Set.of(REFUND_PENDING),
            REFUND_PENDING, Set.of(REFUNDED, SUCCESS),
            FAILED, Set.of(),
            CANCELLED, Set.of(),
            REFUNDED, Set.of()
    );

    public boolean canTransitionTo(PaymentStatus target) {
        if (target == null) {
            return false;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == REFUNDED;
    }
}
