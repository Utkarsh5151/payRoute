package com.payroute.platform.payment.service;

import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.common.exception.ResourceNotFoundException;
import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.payment.dto.RefundRequest;
import com.payroute.platform.payment.dto.RefundResponse;
import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.entity.Refund;
import com.payroute.platform.payment.enums.PaymentStatus;
import com.payroute.platform.payment.enums.RefundStatus;
import com.payroute.platform.payment.repository.PaymentRepository;
import com.payroute.platform.payment.repository.RefundRepository;
import com.payroute.platform.payment.statemachine.PaymentStateMachine;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentStateMachine stateMachine;

    public RefundService(PaymentRepository paymentRepository,
                         RefundRepository refundRepository,
                         PaymentStateMachine stateMachine) {
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.stateMachine = stateMachine;
    }

    @Transactional
    public RefundResponse processRefund(UUID paymentId, RefundRequest request, Merchant merchant, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = refundRepository.findByIdempotencyKeyAndMerchantId(idempotencyKey, merchant.getId());
            if (existing.isPresent()) {
                return RefundResponse.from(existing.get());
            }
        }

        // Pessimistic write lock to prevent race conditions on concurrent refund submissions
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (!payment.getMerchant().getId().equals(merchant.getId())) {
            throw new BadRequestException("Unauthorized: Payment does not belong to this merchant");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.REFUND_PENDING) {
            throw new BadRequestException("Cannot refund payment in state: " + payment.getStatus());
        }

        BigDecimal alreadyRefunded = refundRepository.sumSuccessfulRefundsByPaymentId(paymentId);
        BigDecimal totalAfterRefund = alreadyRefunded.add(request.getAmount());

        if (totalAfterRefund.compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException(String.format(
                    "Refund amount %.2f exceeds remaining refundable balance %.2f",
                    request.getAmount(), payment.getAmount().subtract(alreadyRefunded)
            ));
        }

        Refund refund = new Refund(
                payment,
                merchant,
                request.getAmount(),
                payment.getCurrency(),
                request.getReason(),
                idempotencyKey
        );

        refund.setStatus(RefundStatus.REFUNDED);
        Refund savedRefund = refundRepository.save(refund);

        // If fully refunded, transition to REFUNDED
        if (totalAfterRefund.compareTo(payment.getAmount()) >= 0) {
            stateMachine.transition(payment, PaymentStatus.REFUND_PENDING, "Processing full refund");
            stateMachine.transition(payment, PaymentStatus.REFUNDED, "Full refund completed");
        }

        log.info("Successfully processed refund {} of amount {} for payment {}",
                savedRefund.getId(), request.getAmount(), paymentId);

        return RefundResponse.from(savedRefund);
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsForPayment(UUID paymentId) {
        return refundRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId).stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsForMerchant(UUID merchantId) {
        return refundRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId).stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }
}
