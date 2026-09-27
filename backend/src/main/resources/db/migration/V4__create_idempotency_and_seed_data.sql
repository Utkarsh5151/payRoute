-- V4: Create Idempotency Records and Seed Initial Data

CREATE TABLE IF NOT EXISTS idempotency_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(128) NOT NULL,
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    request_path VARCHAR(128) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_status INTEGER,
    response_body TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_idempotency_merchant_key UNIQUE (merchant_id, idempotency_key)
);

-- Index for scheduled eviction of expired idempotency keys
CREATE INDEX IF NOT EXISTS idx_idempotency_expires_at ON idempotency_records(expires_at);

-- Seed Payment Providers
INSERT INTO payment_providers (id, code, name, status, success_rate, avg_latency_ms, timeout_rate, failure_rate, priority_weight)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'PROVIDER_A', 'ApexPay Gateway', 'ACTIVE', 98.00, 200, 1.00, 1.00, 100),
    ('22222222-2222-2222-2222-222222222222', 'PROVIDER_B', 'NovaPay Switch', 'ACTIVE', 95.00, 150, 3.00, 2.00, 90),
    ('33333333-3333-3333-3333-333333333333', 'PROVIDER_C', 'PulsePay Connect', 'ACTIVE', 90.00, 350, 5.00, 5.00, 80)
ON CONFLICT (code) DO NOTHING;

-- Seed Default Admin User (Password: Admin@123456)
-- Hash generated via standard BCrypt with cost factor 10
INSERT INTO users (id, username, email, password_hash, role)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'admin', 'admin@payroute.dev', '$2a$10$rqcbpwENNL0Kcfvw8n.gJuQgh6q6L8qkY0vBIR4t3XA8VIPIx7Wo2', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- Seed Default Merchant User (Password: Merchant@123456)
INSERT INTO users (id, username, email, password_hash, role)
VALUES
    ('m0000000-0000-0000-0000-000000000001', 'merchant_apex', 'merchant@apex.dev', '$2a$10$NC//DjlAJKhLR0l.MPlP6eKJoWyhYEafkN3CdzJxoCTKdf37DsZCG', 'MERCHANT')
ON CONFLICT (username) DO NOTHING;

-- Seed Default Customer User (Password: User@123456)
INSERT INTO users (id, username, email, password_hash, role)
VALUES
    ('u0000000-0000-0000-0000-000000000001', 'customer_alice', 'alice@customer.dev', '$2a$10$rtHjbub2Antp1gbsuE5zsu0nTjZQ3LOB2Scv8gf11/GFdZP.mNgli', 'USER')
ON CONFLICT (username) DO NOTHING;

-- Seed Default Merchant Entity
INSERT INTO merchants (id, user_id, name, api_key_hash, webhook_url, status)
VALUES
    ('8f8b1b22-1d54-47ef-b209-fa936a2824df', 'm0000000-0000-0000-0000-000000000001', 'Apex Retail Solutions', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'https://webhook.site/payroute-demo', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
