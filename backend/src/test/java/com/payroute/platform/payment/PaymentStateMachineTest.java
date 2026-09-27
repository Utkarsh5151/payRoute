package com.payroute.platform.payment;

import com.payroute.platform.common.exception.InvalidStateTransitionException;
import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.enums.PaymentMethod;
import com.payroute.platform.payment.enums.PaymentStatus;
import com.payroute.platform.payment.repository.PaymentEventRepository;
import com.payroute.platform.payment.repository.PaymentRepository;
import com.payroute.platform.payment.statemachine.PaymentStateMachine;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentStateMachineTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventRepository eventRepository;

    private PaymentStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new PaymentStateMachine(paymentRepository, eventRepository);
    }

    private Payment createSamplePayment(PaymentStatus initialStatus) {
        Payment payment = new Payment(
                new Merchant(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "USD",
                PaymentMethod.CARD,
                "alice@example.com",
                "Test order",
                "ORD-001",
                "idemp-key-123"
        );
        payment.setId(UUID.randomUUID());
        payment.setStatus(initialStatus);
        return payment;
    }

    @Test
    void testAllowedTransitionCreatedToProcessing() {
        Payment payment = createSamplePayment(PaymentStatus.CREATED);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = stateMachine.transition(payment, PaymentStatus.PROCESSING, "Testing lock");

        assertEquals(PaymentStatus.PROCESSING, result.getStatus());
        verify(paymentRepository).save(payment);
        verify(eventRepository).save(any());
    }

    @Test
    void testAllowedTransitionProcessingToSuccess() {
        Payment payment = createSamplePayment(PaymentStatus.PROCESSING);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = stateMachine.transition(payment, PaymentStatus.SUCCESS, "Payment approved");

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void testAllowedTransitionSuccessToRefundPending() {
        Payment payment = createSamplePayment(PaymentStatus.SUCCESS);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = stateMachine.transition(payment, PaymentStatus.REFUND_PENDING, "Refund requested");

        assertEquals(PaymentStatus.REFUND_PENDING, result.getStatus());
    }

    @Test
    void testDisallowedTransitionCreatedToSuccessThrowsException() {
        Payment payment = createSamplePayment(PaymentStatus.CREATED);

        assertThrows(InvalidStateTransitionException.class, () -> {
            stateMachine.transition(payment, PaymentStatus.SUCCESS, "Illegal jump");
        });

        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(eventRepository);
    }

    @Test
    void testDisallowedTransitionFailedToSuccessThrowsException() {
        Payment payment = createSamplePayment(PaymentStatus.FAILED);

        assertThrows(InvalidStateTransitionException.class, () -> {
            stateMachine.transition(payment, PaymentStatus.SUCCESS, "Cannot revive failed payment");
        });
    }

    @Test
    void testDisallowedTransitionRefundedToProcessingThrowsException() {
        Payment payment = createSamplePayment(PaymentStatus.REFUNDED);

        assertThrows(InvalidStateTransitionException.class, () -> {
            stateMachine.transition(payment, PaymentStatus.PROCESSING, "Illegal transition");
        });
    }

    @Test
    void testEnumMatrixDirectly() {
        assertTrue(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.PROCESSING));
        assertTrue(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.CANCELLED));
        assertFalse(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.SUCCESS));
        assertFalse(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.FAILED));

        assertTrue(PaymentStatus.PROCESSING.canTransitionTo(PaymentStatus.SUCCESS));
        assertTrue(PaymentStatus.PROCESSING.canTransitionTo(PaymentStatus.FAILED));
        assertTrue(PaymentStatus.PROCESSING.canTransitionTo(PaymentStatus.PENDING));

        assertTrue(PaymentStatus.SUCCESS.canTransitionTo(PaymentStatus.REFUND_PENDING));
        assertFalse(PaymentStatus.SUCCESS.canTransitionTo(PaymentStatus.CREATED));

        assertTrue(PaymentStatus.REFUND_PENDING.canTransitionTo(PaymentStatus.REFUNDED));
        assertTrue(PaymentStatus.REFUND_PENDING.canTransitionTo(PaymentStatus.SUCCESS));

        assertFalse(PaymentStatus.FAILED.canTransitionTo(PaymentStatus.SUCCESS));
        assertFalse(PaymentStatus.CANCELLED.canTransitionTo(PaymentStatus.PROCESSING));
        assertFalse(PaymentStatus.REFUNDED.canTransitionTo(PaymentStatus.SUCCESS));
    }
}
