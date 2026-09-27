package com.payroute.platform.observability.controller;

import com.payroute.platform.auth.jwt.UserPrincipal;
import com.payroute.platform.common.api.ApiResponse;
import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.common.util.SecurityUtils;
import com.payroute.platform.observability.dto.DashboardStatsDto;
import com.payroute.platform.observability.dto.DashboardStatsDto.ProviderStatusCard;
import com.payroute.platform.payment.dto.PaymentResponse;
import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.enums.PaymentStatus;
import com.payroute.platform.payment.repository.PaymentAttemptRepository;
import com.payroute.platform.payment.repository.PaymentRepository;
import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.provider.repository.PaymentProviderRepository;
import com.payroute.platform.resilience.circuitbreaker.ProviderCircuitBreakerManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Observability & Dashboard", description = "Aggregated KPI metrics, provider health cards, and analytical summaries")
public class DashboardStatsController {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository attemptRepository;
    private final PaymentProviderRepository providerRepository;
    private final ProviderCircuitBreakerManager circuitBreakerManager;

    public DashboardStatsController(PaymentRepository paymentRepository,
                                    PaymentAttemptRepository attemptRepository,
                                    PaymentProviderRepository providerRepository,
                                    ProviderCircuitBreakerManager circuitBreakerManager) {
        this.paymentRepository = paymentRepository;
        this.attemptRepository = attemptRepository;
        this.providerRepository = providerRepository;
        this.circuitBreakerManager = circuitBreakerManager;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get aggregated dashboard statistics, provider metrics, and recent activity")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getStats() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new BadRequestException("User must be authenticated"));

        UUID merchantId = principal.getMerchantId();

        long total = merchantId != null ? paymentRepository.countByMerchantId(merchantId) : paymentRepository.count();
        long successful = merchantId != null ? paymentRepository.countByMerchantIdAndStatus(merchantId, PaymentStatus.SUCCESS) : paymentRepository.countByStatus(PaymentStatus.SUCCESS);
        long failed = merchantId != null ? paymentRepository.countByMerchantIdAndStatus(merchantId, PaymentStatus.FAILED) : paymentRepository.countByStatus(PaymentStatus.FAILED);
        long pending = merchantId != null ? paymentRepository.countByMerchantIdAndStatus(merchantId, PaymentStatus.PROCESSING) : paymentRepository.countByStatus(PaymentStatus.PROCESSING);

        double successRate = total > 0 ? ((double) successful / total) * 100.0 : 100.0;

        // Fetch recent payments
        var pageReq = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Payment> recentList = merchantId != null ?
                paymentRepository.findByMerchantId(merchantId, pageReq).getContent() :
                paymentRepository.findAll(pageReq).getContent();

        List<PaymentResponse> recentDtos = recentList.stream()
                .map(p -> {
                    int attempts = (int) attemptRepository.countByPaymentId(p.getId());
                    return PaymentResponse.from(p, attempts);
                })
                .collect(Collectors.toList());

        BigDecimal volume = recentList.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build provider cards
        List<PaymentProvider> providers = providerRepository.findAll();
        List<ProviderStatusCard> providerCards = providers.stream()
                .map(p -> {
                    String cb = circuitBreakerManager.getCircuitBreakerState(p.getCode());
                    return new ProviderStatusCard(
                            p.getCode(),
                            p.getName(),
                            p.getStatus().name(),
                            p.getSuccessRate().doubleValue(),
                            p.getAvgLatencyMs(),
                            cb
                    );
                })
                .collect(Collectors.toList());

        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalPayments(total);
        stats.setSuccessfulPayments(successful);
        stats.setFailedPayments(failed);
        stats.setPendingPayments(pending);
        stats.setSuccessRatePercentage(Math.round(successRate * 10.0) / 10.0);
        stats.setTotalVolume(volume);
        stats.setProviderCards(providerCards);
        stats.setRecentPayments(recentDtos);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
