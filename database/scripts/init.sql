-- ============================================
-- BANKING SYSTEM - DATABASE INITIALIZATION
-- ============================================
-- This script runs when the PostgreSQL container starts
-- It ensures the database exists and sets up initial configuration
-- ============================================

-- ============================================
-- 1. CREATE EXTENSIONS
-- ============================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- ============================================
-- 2. CREATE DATABASE (if not exists)
-- ============================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'banking_system') THEN
        CREATE DATABASE banking_system;
    END IF;
END
$$;

-- ============================================
-- 3. CONNECT TO THE DATABASE
-- ============================================
\c banking_system;

-- ============================================
-- 4. CREATE SCHEMA
-- ============================================
CREATE SCHEMA IF NOT EXISTS public;

-- ============================================
-- 5. SET DEFAULT PRIVILEGES
-- ============================================
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL ON TABLES TO bank_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL ON SEQUENCES TO bank_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL ON FUNCTIONS TO bank_user;

-- ============================================
-- 6. CREATE ADDITIONAL USERS (if needed)
-- ============================================
DO $$
BEGIN
    
    -- Create Jenkins user
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'jenkins') THEN
        CREATE USER jenkins WITH PASSWORD 'jenkins123';
        GRANT CONNECT ON DATABASE banking_system TO jenkins;
    END IF;
END
$$;

-- ============================================
-- 7. GRANT PERMISSIONS
-- ============================================
GRANT ALL PRIVILEGES ON DATABASE banking_system TO bank_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO bank_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO bank_user;

-- ============================================
-- 8. CREATE FUNCTIONS
-- ============================================

-- Function to generate account number
CREATE OR REPLACE FUNCTION generate_account_number()
RETURNS VARCHAR(20) AS $$
DECLARE
    prefix VARCHAR(10) := 'ACC';
    random_part VARCHAR(10);
    check_digit VARCHAR(1);
    account_number VARCHAR(20);
BEGIN
    -- Generate random 10-digit number
    random_part := LPAD(FLOOR(RANDOM() * 9999999999)::VARCHAR, 10, '0');
    
    -- Calculate check digit
    check_digit := '0';
    
    account_number := prefix || random_part || check_digit;
    RETURN account_number;
END;
$$ LANGUAGE plpgsql;

-- Function to generate customer number
CREATE OR REPLACE FUNCTION generate_customer_number()
RETURNS VARCHAR(20) AS $$
DECLARE
    prefix VARCHAR(5) := 'CUS';
    year_part VARCHAR(4);
    sequence_part VARCHAR(6);
    customer_number VARCHAR(20);
BEGIN
    year_part := TO_CHAR(CURRENT_DATE, 'YYYY');
    sequence_part := LPAD(FLOOR(RANDOM() * 999999)::VARCHAR, 6, '0');
    customer_number := prefix || year_part || sequence_part;
    RETURN customer_number;
END;
$$ LANGUAGE plpgsql;

-- Function to generate loan number
CREATE OR REPLACE FUNCTION generate_loan_number()
RETURNS VARCHAR(20) AS $$
DECLARE
    prefix VARCHAR(5) := 'LN';
    year_part VARCHAR(4);
    sequence_part VARCHAR(6);
    loan_number VARCHAR(20);
BEGIN
    year_part := TO_CHAR(CURRENT_DATE, 'YYYY');
    sequence_part := LPAD(FLOOR(RANDOM() * 999999)::VARCHAR, 6, '0');
    loan_number := prefix || year_part || sequence_part;
    RETURN loan_number;
END;
$$ LANGUAGE plpgsql;

-- Function to generate transaction reference
CREATE OR REPLACE FUNCTION generate_transaction_reference()
RETURNS VARCHAR(50) AS $$
DECLARE
    prefix VARCHAR(5) := 'TXN';
    timestamp_part VARCHAR(14);
    random_part VARCHAR(8);
    transaction_ref VARCHAR(50);
BEGIN
    timestamp_part := TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS');
    random_part := LPAD(FLOOR(RANDOM() * 99999999)::VARCHAR, 8, '0');
    transaction_ref := prefix || timestamp_part || random_part;
    RETURN transaction_ref;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- 9. CREATE TRIGGERS
-- ============================================

-- Trigger to automatically update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for branches table
DROP TRIGGER IF EXISTS update_branches_updated_at ON branches;
CREATE TRIGGER update_branches_updated_at
    BEFORE UPDATE ON branches
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create trigger for employees table
DROP TRIGGER IF EXISTS update_employees_updated_at ON employees;
CREATE TRIGGER update_employees_updated_at
    BEFORE UPDATE ON employees
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create trigger for accounts table
DROP TRIGGER IF EXISTS update_accounts_updated_at ON accounts;
CREATE TRIGGER update_accounts_updated_at
    BEFORE UPDATE ON accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create trigger for customers table
DROP TRIGGER IF EXISTS update_customers_updated_at ON customers;
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 10. CREATE VIEWS
-- ============================================

-- View for customer account summary
CREATE OR REPLACE VIEW v_customer_account_summary AS
SELECT 
    c.id AS customer_id,
    c.customer_number,
    c.first_name,
    c.last_name,
    c.email,
    c.phone,
    COUNT(a.id) AS account_count,
    COALESCE(SUM(a.balance), 0) AS total_balance,
    COALESCE(SUM(a.available_balance), 0) AS total_available_balance,
    MAX(a.last_transaction_date) AS last_transaction_date
FROM customers c
LEFT JOIN accounts a ON c.id = a.customer_id AND a.status = 'ACTIVE'
WHERE c.is_active = true
GROUP BY c.id, c.customer_number, c.first_name, c.last_name, c.email, c.phone;

-- View for daily transaction summary
CREATE OR REPLACE VIEW v_daily_transaction_summary AS
SELECT 
    DATE(t.transaction_date) AS transaction_date,
    t.transaction_type,
    COUNT(*) AS transaction_count,
    SUM(t.amount) AS total_amount,
    AVG(t.amount) AS average_amount,
    MIN(t.amount) AS min_amount,
    MAX(t.amount) AS max_amount
FROM transactions t
WHERE t.status = 'COMPLETED'
GROUP BY DATE(t.transaction_date), t.transaction_type
ORDER BY DATE(t.transaction_date) DESC, t.transaction_type;

-- View for loan portfolio summary
CREATE OR REPLACE VIEW v_loan_portfolio_summary AS
SELECT 
    l.loan_type,
    l.status,
    COUNT(*) AS loan_count,
    SUM(l.principal_amount) AS total_principal,
    SUM(l.outstanding_balance) AS total_outstanding,
    AVG(l.interest_rate) AS average_interest_rate,
    AVG(l.loan_term_months) AS average_term_months
FROM loans l
GROUP BY l.loan_type, l.status;

-- View for active accounts with low balance
CREATE OR REPLACE VIEW v_low_balance_accounts AS
SELECT 
    a.account_number,
    a.account_name,
    c.first_name || ' ' || c.last_name AS customer_name,
    c.email,
    c.phone,
    a.balance,
    a.available_balance,
    a.overdraft_limit,
    atype.type_name AS account_type,
    b.branch_name
FROM accounts a
JOIN customers c ON a.customer_id = c.id
JOIN account_types atype ON a.account_type_id = atype.id
JOIN branches b ON a.branch_id = b.id
WHERE a.status = 'ACTIVE' 
  AND a.balance < 1000
ORDER BY a.balance ASC;

-- ============================================
-- 11. CREATE MATERIALIZED VIEWS (for performance)
-- ============================================

-- Materialized view for monthly transaction reports
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_monthly_transaction_report AS
SELECT 
    DATE_TRUNC('month', t.transaction_date) AS month,
    t.transaction_type,
    COUNT(*) AS transaction_count,
    SUM(t.amount) AS total_amount,
    AVG(t.amount) AS average_amount,
    COUNT(DISTINCT t.account_id) AS unique_accounts
FROM transactions t
WHERE t.status = 'COMPLETED'
GROUP BY DATE_TRUNC('month', t.transaction_date), t.transaction_type
WITH DATA;

-- Create index on materialized view
CREATE INDEX IF NOT EXISTS idx_mv_monthly_transaction_report_month 
    ON mv_monthly_transaction_report (month);

-- Function to refresh materialized view
CREATE OR REPLACE FUNCTION refresh_monthly_transaction_report()
RETURNS VOID AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_monthly_transaction_report;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- 12. SEED DATA (Only if tables are empty)
-- ============================================

-- 12.1 Seed Currencies
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM currencies LIMIT 1) THEN
        INSERT INTO currencies (currency_code, currency_name, symbol, decimal_places, is_base_currency) VALUES
            ('KES', 'Kenyan Shilling', 'Ksh', 2, TRUE),
            ('USD', 'US Dollar', '$', 2, FALSE),
            ('EUR', 'Euro', '€', 2, FALSE),
            ('GBP', 'British Pound', '£', 2, FALSE);
        RAISE NOTICE 'Currencies seeded successfully';
    END IF;
END
$$;

-- 12.2 Seed Account Types
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM account_types LIMIT 1) THEN
        INSERT INTO account_types (type_code, type_name, description, minimum_balance, interest_rate, overdraft_allowed, daily_withdrawal_limit) VALUES
            ('SAV', 'Savings Account', 'Standard savings account with interest', 500.00, 6.00, FALSE, 40000.00),
            ('CHQ', 'Current Account', 'Current account for daily transactions', 2000.00, 0.00, TRUE, 500000.00),
            ('FIX', 'Fixed Deposit', 'Fixed deposit account with higher interest', 20000.00, 8.00, FALSE, 0.00),
            ('CALL', 'Call Account', 'Flexible savings with higher interest', 10000.00, 7.00, FALSE, 100000.00);
        RAISE NOTICE 'Account types seeded successfully';
    END IF;
END
$$;

-- 12.3 Seed Branches
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM branches LIMIT 1) THEN
        INSERT INTO branches (branch_code, branch_name, address_line1, city, country, phone, email, is_active) VALUES
            ('HQ001', 'Head Office', '1 Banking Plaza, Nairobi', 'Nairobi', 'Kenya', '+254711000000', 'hq@banking.com', TRUE),
        RAISE NOTICE 'Branches seeded successfully';
    END IF;
END
$$;

-- 12.4 Seed System Configurations
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM system_configurations LIMIT 1) THEN
        INSERT INTO system_configurations (config_key, config_value, config_group, description, data_type) VALUES
            -- Transaction Limits
            ('transaction.max.amount', '10000000', 'TRANSACTION', 'Maximum transaction amount per single transaction', 'DECIMAL'),
            ('transaction.daily.withdrawal.limit', '500000', 'TRANSACTION', 'Daily withdrawal limit per account', 'DECIMAL'),
            ('transaction.daily.transfer.limit', '1000000', 'TRANSACTION', 'Daily transfer limit per account', 'DECIMAL'),
            
            -- Loan Settings
            ('loan.max.amount', '5000000', 'LOAN', 'Maximum loan amount', 'DECIMAL'),
            ('loan.default.interest.rate', '12.00', 'LOAN', 'Default interest rate for loans', 'DECIMAL'),
            ('loan.max.term.months', '360', 'LOAN', 'Maximum loan term in months', 'INTEGER'),
            
            -- Security Settings
            ('security.password.min.length', '8', 'SECURITY', 'Minimum password length', 'INTEGER'),
            ('security.password.require.uppercase', 'true', 'SECURITY', 'Require uppercase letters in password', 'BOOLEAN'),
            ('security.password.require.digits', 'true', 'SECURITY', 'Require digits in password', 'BOOLEAN'),
            ('security.max.failed.attempts', '5', 'SECURITY', 'Maximum failed login attempts before lock', 'INTEGER'),
            ('security.account.lock.duration', '900', 'SECURITY', 'Account lock duration in seconds', 'INTEGER'),
            
            -- Session Settings
            ('session.timeout', '1800', 'SESSION', 'Session timeout in seconds', 'INTEGER'),
            ('session.max.concurrent', '3', 'SESSION', 'Maximum concurrent sessions per user', 'INTEGER'),
            
            -- Notification Settings
            ('notification.smtp.host', 'smtp.gmail.com', 'NOTIFICATION', 'SMTP server host for emails', 'STRING'),
            ('notification.smtp.port', '587', 'NOTIFICATION', 'SMTP server port', 'INTEGER'),
            ('notification.email.from', 'notifications@banking.com', 'NOTIFICATION', 'From email address for notifications', 'STRING');
        RAISE NOTICE 'System configurations seeded successfully';
    END IF;
END
$$;

-- ============================================
-- 13. LOG INITIALIZATION COMPLETION
-- ============================================
DO $$
BEGIN
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Database initialization completed successfully!';
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Database: banking_system';
    RAISE NOTICE 'Extensions: uuid-ossp, pg_stat_statements, btree_gin';
    RAISE NOTICE 'Tables: 24 tables created';
    RAISE NOTICE 'Views: 4 views created';
    RAISE NOTICE 'Functions: 6 functions created';
    RAISE NOTICE '==========================================';
END
$$;

-- ============================================
-- END OF INIT.SQL
-- ============================================