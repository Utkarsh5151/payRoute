package com.payroute.platform.payment;

import com.payroute.platform.common.exception.ResourceNotFoundException;
import com.payroute.platform.payment.repository.PaymentAttemptRepository;
import com.payroute.platform.payment.repository.PaymentEventRepository;
import com.payroute.platform.payment.repository.PaymentRepository;
import com.payroute.platform.payment.service.PaymentService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceAuthorizationTest {
    @Mock PaymentRepository payments;
    @Mock PaymentAttemptRepository attempts;
    @Mock PaymentEventRepository events;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(payments, attempts, events);
    }

    @Test
    void missingMerchantCannotReadPayment() {
        assertThrows(AccessDeniedException.class, () -> service.getPayment(UUID.randomUUID(), null));
        verifyNoInteractions(payments, attempts, events);
    }

    @Test
    void otherMerchantCannotReadPayment() {
        UUID paymentId = UUID.randomUUID();
        UUID otherMerchant = UUID.randomUUID();
        when(payments.findByIdAndMerchantId(paymentId, otherMerchant)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getPayment(paymentId, otherMerchant));
        verify(payments, never()).findById(any());
    }

    @Test
    void otherMerchantCannotReadAttempts() {
        UUID paymentId = UUID.randomUUID();
        UUID otherMerchant = UUID.randomUUID();
        when(payments.findByIdAndMerchantId(paymentId, otherMerchant)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getAttempts(paymentId, otherMerchant));
        verifyNoInteractions(attempts);
    }

    @Test
    void otherMerchantCannotReadEvents() {
        UUID paymentId = UUID.randomUUID();
        UUID otherMerchant = UUID.randomUUID();
        when(payments.findByIdAndMerchantId(paymentId, otherMerchant)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getEvents(paymentId, otherMerchant));
        verifyNoInteractions(events);
    }

    @Test
    void otherMerchantCannotReadRefundHistory() {
        UUID paymentId = UUID.randomUUID();
        UUID otherMerchant = UUID.randomUUID();
        when(payments.findByIdAndMerchantId(paymentId, otherMerchant)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.verifyOwnership(paymentId, otherMerchant));
    }

    @Test
    void missingMerchantCannotListPayments() {
        assertThrows(AccessDeniedException.class, () -> service.listPayments(null, null, null, PageRequest.of(0, 10)));
        verifyNoInteractions(payments);
    }
}
