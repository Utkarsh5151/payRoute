package com.payroute.platform.payment;

import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.common.exception.DuplicateIdempotencyException;
import com.payroute.platform.merchant.entity.Merchant;
import com.payroute.platform.payment.dto.CreatePaymentRequest;
import com.payroute.platform.payment.enums.PaymentMethod;
import com.payroute.platform.payment.idempotency.IdempotencyRecord;
import com.payroute.platform.payment.idempotency.IdempotencyRecordRepository;
import com.payroute.platform.payment.idempotency.IdempotencyService;
import com.payroute.platform.payment.idempotency.IdempotencyStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyRecordRepository repository;

    private IdempotencyService service;
    private Merchant merchant;

    @BeforeEach
    void setUp() {
        service = new IdempotencyService(repository);
        merchant = new Merchant();
        merchant.setId(UUID.randomUUID());
    }

    @Test
    void testNewKeyAcquiresLock() {
        String key = "test-key-001";
        CreatePaymentRequest req = new CreatePaymentRequest(
                new BigDecimal("50.00"), "USD", PaymentMethod.CARD, "alice@test.com", "ORD-1", "Desc"
        );

        when(repository.findByMerchantIdAndIdempotencyKey(merchant.getId(), key))
                .thenReturn(Optional.empty());

        Optional<IdempotencyRecord> result = service.checkOrLock(key, merchant, "/api/payments", req);

        assertTrue(result.isEmpty(), "New key should not return an existing record");
        verify(repository).saveAndFlush(any(IdempotencyRecord.class));
    }

    @Test
    void testConcurrentInProgressKeyThrowsDuplicateIdempotencyException() {
        String key = "test-key-in-progress";
        CreatePaymentRequest req = new CreatePaymentRequest(
                new BigDecimal("50.00"), "USD", PaymentMethod.CARD, "alice@test.com", "ORD-1", "Desc"
        );

        IdempotencyRecord inProgressRecord = new IdempotencyRecord(key, merchant, "/api/payments",
                "dummyhash", Instant.now().plusSeconds(3600));
        inProgressRecord.setStatus(IdempotencyStatus.IN_PROGRESS);

        // compute expected hash
        String hash = com.payroute.platform.common.util.HashUtils.sha256(
                com.payroute.platform.common.util.JsonUtils.toJson(req)
        );
        inProgressRecord.setRequestHash(hash);

        when(repository.findByMerchantIdAndIdempotencyKey(merchant.getId(), key))
                .thenReturn(Optional.of(inProgressRecord));

        assertThrows(DuplicateIdempotencyException.class, () -> {
            service.checkOrLock(key, merchant, "/api/payments", req);
        });
    }

    @Test
    void testReusedKeyWithMismatchedPayloadThrowsBadRequestException() {
        String key = "test-key-reused";
        CreatePaymentRequest req1 = new CreatePaymentRequest(
                new BigDecimal("50.00"), "USD", PaymentMethod.CARD, "alice@test.com", "ORD-1", "Desc"
        );
        CreatePaymentRequest req2 = new CreatePaymentRequest(
                new BigDecimal("99.99"), "USD", PaymentMethod.CARD, "alice@test.com", "ORD-1", "Desc"
        );

        IdempotencyRecord existing = new IdempotencyRecord(key, merchant, "/api/payments",
                "hash-of-first-request", Instant.now().plusSeconds(3600));
        existing.setStatus(IdempotencyStatus.COMPLETED);

        when(repository.findByMerchantIdAndIdempotencyKey(merchant.getId(), key))
                .thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () -> {
            service.checkOrLock(key, merchant, "/api/payments", req2);
        });
    }
}
