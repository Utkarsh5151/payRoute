-- V2: Create Payment Providers and Payments Tables

CREATE TABLE IF NOT EXISTS payment_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    success_rate NUMERIC(5,2) NOT NULL DEFAULT 98.00,
    avg_latency_ms INTEGER NOT NULL DEFAULT 250,
    timeout_rate NUMERIC(5,2) NOT NULL DEFAULT 2.00,
    failure_rate NUMERIC(5,2) NOT NULL DEFAULT 2.00,
    priority_weight INTEGER NOT NULL DEFAULT 100,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    user_id UUID REFERENCES users(id),
    amount NUMERIC(18,4) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_method VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    idempotency_key VARCHAR(128),
    client_reference_id VARCHAR(128),
    customer_email VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    selected_provider_id UUID REFERENCES payment_providers(id),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for merchant payment listing (paginated by creation date)
CREATE INDEX IF NOT EXISTS idx_payments_merchant_created ON payments(merchant_id, created_at DESC);

-- Index for payment status monitoring & filtering
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);

-- Composite index for merchant client reconciliation
CREATE INDEX IF NOT EXISTS idx_payments_client_ref ON payments(client_reference_id, merchant_id);
