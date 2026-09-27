package com.payroute.platform.payment.statemachine;

import com.payroute.platform.common.exception.InvalidStateTransitionException;
import com.payroute.platform.common.util.JsonUtils;
import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.entity.PaymentEvent;
import com.payroute.platform.payment.enums.EventType;
import com.payroute.platform.payment.enums.PaymentStatus;
import com.payroute.platform.payment.repository.PaymentEventRepository;
import com.payroute.platform.payment.repository.PaymentRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentStateMachine {

    private static final Logger log = LoggerFactory.getLogger(PaymentStateMachine.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository eventRepository;

    public PaymentStateMachine(PaymentRepository paymentRepository, PaymentEventRepository eventRepository) {
        this.paymentRepository = paymentRepository;
        this.eventRepository = eventRepository;
    }

    public Payment transition(Payment payment, PaymentStatus targetStatus, String reason) {
        PaymentStatus current = payment.getStatus();

        if (current == targetStatus) {
            return payment;
        }

        if (!current.canTransitionTo(targetStatus)) {
            log.error("Illegal payment state transition from {} to {} for payment {}",
                    current, targetStatus, payment.getId());
            throw new InvalidStateTransitionException(current.name(), targetStatus.name());
        }

        log.info("Transitioning payment {} from {} -> {} (reason: {})",
                payment.getId(), current, targetStatus, reason);

        payment.setStatus(targetStatus);
        Payment saved = paymentRepository.save(payment);

        String payloadJson = JsonUtils.toJson(Map.of(
                "fromStatus", current.name(),
                "toStatus", targetStatus.name(),
                "reason", reason != null ? reason : "State machine transition"
        ));

        PaymentEvent event = new PaymentEvent(saved, EventType.PAYMENT_STATUS_CHANGED, current, targetStatus, payloadJson);
        eventRepository.save(event);

        return saved;
    }
}
