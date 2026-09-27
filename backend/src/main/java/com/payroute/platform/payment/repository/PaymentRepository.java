package com.payroute.platform.payment.repository;

import com.payroute.platform.payment.entity.Payment;
import com.payroute.platform.payment.enums.PaymentMethod;
import com.payroute.platform.payment.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByMerchantId(UUID merchantId, Pageable pageable);

    Page<Payment> findByMerchantIdAndStatus(UUID merchantId, PaymentStatus status, Pageable pageable);

    Page<Payment> findByMerchantIdAndPaymentMethod(UUID merchantId, PaymentMethod paymentMethod, Pageable pageable);

    Page<Payment> findByMerchantIdAndStatusAndPaymentMethod(UUID merchantId, PaymentStatus status, PaymentMethod paymentMethod, Pageable pageable);

    Optional<Payment> findByIdAndMerchantId(UUID id, UUID merchantId);

    Optional<Payment> findByIdempotencyKeyAndMerchantId(String idempotencyKey, UUID merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") UUID id);

    long countByStatus(PaymentStatus status);

    long countByMerchantId(UUID merchantId);

    long countByMerchantIdAndStatus(UUID merchantId, PaymentStatus status);
}
