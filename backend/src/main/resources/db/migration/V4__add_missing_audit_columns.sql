-- V4: Add missing audit columns


-- daily_transaction_limits
ALTER TABLE daily_transaction_limits
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(50),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(50);

-- account_joint_holders
ALTER TABLE account_joint_holders
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(50),
    ADD COLUMN IF NOT EXISTS version    BIGINT DEFAULT 0 NOT NULL;

-- exchange_rates
ALTER TABLE exchange_rates
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

-- loan_payments
ALTER TABLE loan_payments
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(50),
    ADD COLUMN IF NOT EXISTS version    BIGINT DEFAULT 0 NOT NULL;

-- password_reset_tokens
ALTER TABLE password_reset_tokens
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(50);

-- audit_logs
ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(50),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(50);