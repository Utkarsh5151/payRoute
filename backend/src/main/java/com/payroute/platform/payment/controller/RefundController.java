package com.payroute.platform.payment.controller;

import com.payroute.platform.auth.jwt.UserPrincipal;
import com.payroute.platform.common.api.ApiResponse;
import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.common.util.SecurityUtils;
import com.payroute.platform.payment.dto.RefundResponse;
import com.payroute.platform.payment.service.RefundService;
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

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    @Operation(summary = "List all refunds issued by current merchant")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> listMerchantRefunds() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new BadRequestException("User must be authenticated"));

        List<RefundResponse> refunds = refundService.getRefundsForMerchant(principal.getMerchantId());
        return ResponseEntity.ok(ApiResponse.success(refunds));
    }
}
