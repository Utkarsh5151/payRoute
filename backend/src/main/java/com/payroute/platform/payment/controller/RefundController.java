package com.payroute.platform.payment.controller;

import com.payroute.platform.auth.jwt.UserPrincipal;
import com.payroute.platform.common.api.ApiResponse;
import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.common.util.SecurityUtils;
import com.payroute.platform.payment.dto.RefundResponse;
import com.payroute.platform.payment.service.RefundService;
import com.payroute.platform.merchant.service.MerchantService;
import com.payroute.platform.merchant.entity.Merchant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/refunds")
@Tag(name = "Refunds", description = "Merchant refund management and query endpoints")
public class RefundController {

    private final RefundService refundService;
    private final MerchantService merchantService;

    public RefundController(RefundService refundService, MerchantService merchantService) {
        this.refundService = refundService;
        this.merchantService = merchantService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    @Operation(summary = "List all refunds issued by current merchant")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> listMerchantRefunds() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new BadRequestException("User must be authenticated"));

        List<RefundResponse> refunds = refundService.getRefundsForMerchant(merchantService.findByUserId(principal.getId())
                .map(Merchant::getId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Merchant account required")));
        return ResponseEntity.ok(ApiResponse.success(refunds));
    }
}
