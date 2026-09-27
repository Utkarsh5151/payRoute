package com.payroute.platform.payment.repository;

import com.payroute.platform.payment.entity.Refund;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    List<Refund> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId);

    List<Refund> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);

    Optional<Refund> findByIdempotencyKeyAndMerchantId(String idempotencyKey, UUID merchantId);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.payment.id = :paymentId AND r.status = 'REFUNDED'")
    BigDecimal sumSuccessfulRefundsByPaymentId(@Param("paymentId") UUID paymentId);
}
