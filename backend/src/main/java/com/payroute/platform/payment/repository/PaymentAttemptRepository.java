package com.payroute.platform.payment.repository;

import com.payroute.platform.payment.entity.PaymentAttempt;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {

    List<PaymentAttempt> findByPaymentIdOrderByAttemptNumberAsc(UUID paymentId);

    long countByPaymentId(UUID paymentId);

    List<PaymentAttempt> findTop50ByProviderIdOrderByStartedAtDesc(UUID providerId);
}
