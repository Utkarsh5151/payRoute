package com.payroute.platform.payment.service;

import com.payroute.platform.common.api.PageResponse;
import com.payroute.platform.common.exception.ResourceNotFoundException;
import com.payroute.platform.payment.dto.PaymentAttemptDto;
import com.payroute.platform.payment.dto.PaymentEventDto;
import com.payroute.platform.payment.dto.PaymentResponse;
import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.enums.PaymentMethod;
import com.payroute.platform.payment.enums.PaymentStatus;
import com.payroute.platform.payment.repository.PaymentAttemptRepository;
import com.payroute.platform.payment.repository.PaymentEventRepository;
import com.payroute.platform.payment.repository.PaymentRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository attemptRepository;
    private final PaymentEventRepository eventRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentAttemptRepository attemptRepository,
                          PaymentEventRepository eventRepository) {
        this.paymentRepository = paymentRepository;
        this.attemptRepository = attemptRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID id, UUID merchantId) {
        Payment payment = requireOwnedPayment(id, merchantId);

        int attempts = (int) attemptRepository.countByPaymentId(payment.getId());
        return PaymentResponse.from(payment, attempts);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> listPayments(UUID merchantId, PaymentStatus status,
                                                      PaymentMethod method, Pageable pageable) {
        requireMerchantId(merchantId);
        Page<Payment> page;

        if (status != null && method != null) {
            page = paymentRepository.findByMerchantIdAndStatusAndPaymentMethod(merchantId, status, method, pageable);
        } else if (status != null) {
            page = paymentRepository.findByMerchantIdAndStatus(merchantId, status, pageable);
        } else if (method != null) {
            page = paymentRepository.findByMerchantIdAndPaymentMethod(merchantId, method, pageable);
        } else {
            page = paymentRepository.findByMerchantId(merchantId, pageable);
        }

        List<PaymentResponse> content = page.getContent().stream()
                .map(p -> {
                    int attempts = (int) attemptRepository.countByPaymentId(p.getId());
                    return PaymentResponse.from(p, attempts);
                })
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<PaymentAttemptDto> getAttempts(UUID paymentId, UUID merchantId) {
        requireOwnedPayment(paymentId, merchantId);
        return attemptRepository.findByPaymentIdOrderByAttemptNumberAsc(paymentId).stream()
                .map(PaymentAttemptDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentEventDto> getEvents(UUID paymentId, UUID merchantId) {
        requireOwnedPayment(paymentId, merchantId);
        return eventRepository.findByPaymentIdOrderByCreatedAtAsc(paymentId).stream()
                .map(PaymentEventDto::from)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public void verifyOwnership(UUID paymentId, UUID merchantId) {
        requireOwnedPayment(paymentId, merchantId);
    }

    private Payment requireOwnedPayment(UUID paymentId, UUID merchantId) {
        requireMerchantId(merchantId);
        return paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
    }

    private void requireMerchantId(UUID merchantId) {
        if (merchantId == null) {
            throw new org.springframework.security.access.AccessDeniedException("Merchant identity is required");
        }
    }
}
