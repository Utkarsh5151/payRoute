package com.payroute.platform.payment.idempotency;

import com.payroute.platform.common.exception.BadRequestException;
import com.payroute.platform.common.exception.DuplicateIdempotencyException;
import com.payroute.platform.common.util.HashUtils;
import com.payroute.platform.common.util.JsonUtils;
import com.payroute.platform.merchant.entity.Merchant;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final IdempotencyRecordRepository repository;

    public IdempotencyService(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    /**
     * Checks if an idempotency key exists or acquires an in-progress lock record.
     * Uses REQUIRES_NEW so the lock record is committed immediately and visible across concurrent threads.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<IdempotencyRecord> checkOrLock(String key, Merchant merchant, String requestPath, Object requestPayload) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }

        String requestJson = JsonUtils.toJson(requestPayload);
        String requestHash = HashUtils.sha256(requestJson);

        Optional<IdempotencyRecord> existing = repository.findByMerchantIdAndIdempotencyKey(merchant.getId(), key);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw new BadRequestException("Idempotency key '" + key + "' was previously used with a different request payload.");
            }
            if (record.getStatus() == IdempotencyStatus.IN_PROGRESS) {
                throw new DuplicateIdempotencyException(key, "A request with idempotency key '" + key + "' is currently being processed.");
            }
            // Record is COMPLETED, return it for replay
            return Optional.of(record);
        }

        try {
            IdempotencyRecord newRecord = new IdempotencyRecord(
                    key,
                    merchant,
                    requestPath,
                    requestHash,
                    Instant.now().plus(DEFAULT_TTL)
            );
            repository.saveAndFlush(newRecord);
            return Optional.empty();
        } catch (DataIntegrityViolationException e) {
            // Concurrent race condition hit DB unique constraint (merchant_id, idempotency_key)
            log.warn("Concurrent idempotency lock race condition detected for key: {}", key);
            throw new DuplicateIdempotencyException(key, "Concurrent request detected for idempotency key: " + key);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String key, UUID merchantId, int statusCode, Object responsePayload) {
        if (key == null || key.isBlank()) {
            return;
        }

        repository.findByMerchantIdAndIdempotencyKey(merchantId, key).ifPresent(record -> {
            record.setStatus(IdempotencyStatus.COMPLETED);
            record.setResponseStatus(statusCode);
            record.setResponseBody(JsonUtils.toJson(responsePayload));
            repository.save(record);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String key, UUID merchantId) {
        if (key == null || key.isBlank()) {
            return;
        }

        repository.findByMerchantIdAndIdempotencyKey(merchantId, key).ifPresent(record -> {
            record.setStatus(IdempotencyStatus.FAILED);
            repository.save(record);
        });
    }
}
