package com.payroute.platform.payment.controller;

import com.payroute.platform.auth.jwt.UserPrincipal;
import com.payroute.platform.common.api.ApiResponse;
import com.payroute.platform.common.api.PageResponse;
import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.common.util.SecurityUtils;
import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.merchant.service.MerchantService;
import com.payroute.platform.payment.dto.*;
import com.payroute.platform.payment.enums.PaymentMethod;
import com.payroute.platform.payment.enums.PaymentStatus;
import com.payroute.platform.payment.service.PaymentOrchestrator;
import com.payroute.platform.payment.service.PaymentService;
import com.payroute.platform.payment.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Core payment creation, execution orchestration, and audit logs")
public class PaymentController {

    private final PaymentOrchestrator orchestrator;
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final MerchantService merchantService;

    public PaymentController(PaymentOrchestrator orchestrator,
                             PaymentService paymentService,
                             RefundService refundService,
                             MerchantService merchantService) {
        this.orchestrator = orchestrator;
        this.paymentService = paymentService;
        this.refundService = refundService;
        this.merchantService = merchantService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'USER')")
    @Operation(summary = "Create and execute payment through intelligent orchestration pipeline")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) {

        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new BadRequestException("User must be authenticated"));

        Merchant merchant = null;
        if (principal.getMerchantId() != null) {
            merchant = merchantService.getById(principal.getMerchantId());
        } else {
            merchant = merchantService.findByUserId(principal.getId())
                    .orElseGet(() -> merchantService.createMerchant(null, principal.getUsername() + " Store", null));
        }

        PaymentResponse response = orchestrator.processPayment(request, merchant, principal.getId(), idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve payment details by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal().orElse(null);
        UUID merchantId = principal != null ? principal.getMerchantId() : null;
        PaymentResponse response = paymentService.getPayment(id, merchantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List paginated payments with optional status and method filters")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> listPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new BadRequestException("User must be authenticated"));

        UUID merchantId = principal.getMerchantId();
        if (merchantId == null) {
            merchantId = merchantService.findByUserId(principal.getId()).map(Merchant::getId).orElse(null);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<PaymentResponse> result = paymentService.listPayments(merchantId, status, paymentMethod, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}/attempts")
    @Operation(summary = "Get audit trail of provider invocation attempts for a payment")
    public ResponseEntity<ApiResponse<List<PaymentAttemptDto>>> getPaymentAttempts(@PathVariable UUID id) {
        List<PaymentAttemptDto> attempts = paymentService.getAttempts(id);
        return ResponseEntity.ok(ApiResponse.success(attempts));
    }

    @GetMapping("/{id}/events")
    @Operation(summary = "Get state transition event timeline for a payment")
    public ResponseEntity<ApiResponse<List<PaymentEventDto>>> getPaymentEvents(@PathVariable UUID id) {
        List<PaymentEventDto> events = paymentService.getEvents(id);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    @Operation(summary = "Initiate a partial or full refund on a successful payment")
    public ResponseEntity<ApiResponse<RefundResponse>> refundPayment(
            @PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody RefundRequest request) {

        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new BadRequestException("User must be authenticated"));

        Merchant merchant = merchantService.getById(principal.getMerchantId());
        RefundResponse response = refundService.processRefund(id, request, merchant, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}/refunds")
    @Operation(summary = "Get list of refunds associated with a payment")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getPaymentRefunds(@PathVariable UUID id) {
        List<RefundResponse> refunds = refundService.getRefundsForPayment(id);
        return ResponseEntity.ok(ApiResponse.success(refunds));
    }
}
