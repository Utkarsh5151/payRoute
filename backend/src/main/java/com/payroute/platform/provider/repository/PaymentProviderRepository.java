package com.payroute.platform.provider.repository;

import com.payroute.platform.provider.entity.PaymentProvider;
import com.payroute.platform.provider.enums.ProviderStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentProviderRepository extends JpaRepository<PaymentProvider, UUID> {

    Optional<PaymentProvider> findByCode(String code);

    List<PaymentProvider> findByStatus(ProviderStatus status);
}
