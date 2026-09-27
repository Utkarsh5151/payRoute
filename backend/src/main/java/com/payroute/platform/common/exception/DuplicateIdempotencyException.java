package com.payroute.platform.common.exception;

public class DuplicateIdempotencyException extends RuntimeException {

    private final String idempotencyKey;

    public DuplicateIdempotencyException(String idempotencyKey, String message) {
        super(message);
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
