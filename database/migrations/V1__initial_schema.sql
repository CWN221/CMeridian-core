-- BANKING SYSTEM DATABASE SCHEMA
-- 
-- Enterprise Banking System - Initial Schema
-- PostgreSQL 15+

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. BRANCHES TABLE
CREATE TABLE branches (
    id BIGSERIAL PRIMARY KEY,
    branch_code VARCHAR(20) UNIQUE NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50),
    postal_code VARCHAR(50),
    country VARCHAR(50),
    phone VARCHAR(50),
    email VARCHAR(100),
    manager_employee_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- 2. EMPLOYEES TABLE
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_no VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    middle_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(50),
    branch_id BIGINT NOT NULL,
    department VARCHAR(50),
    position VARCHAR(50),
    hire_date DATE NOT NULL,
    termination_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_employee_branch FOREIGN KEY (branch_id)
        REFERENCES branches(id) ON DELETE RESTRICT,
    CONSTRAINT chk_employment_dates CHECK (termination_date IS NULL OR termination_date >= hire_date)
);

-- 3. PERMISSIONS TABLE
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(50) UNIQUE NOT NULL,
    permission_description VARCHAR(200),
    resource_type VARCHAR(100),
    action VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
);

-- 4. ROLES TABLE
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    role_description VARCHAR(200),
    is_system_role BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- 5. ROLE PERMISSIONS TABLE
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by VARCHAR(50),
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id)
        REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id)
        REFERENCES permissions(id) ON DELETE CASCADE
);

-- 6. USERS TABLE (System Users)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    employee_id BIGINT UNIQUE NOT NULL,
    is_account_locked BOOLEAN DEFAULT FALSE,
    is_credentials_expired BOOLEAN DEFAULT FALSE,
    is_enabled BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    failed_login_attempts INT DEFAULT 0,
    account_locked_until TIMESTAMP,
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_user_employee FOREIGN KEY (employee_id)
        REFERENCES employees(id) ON DELETE RESTRICT
);

-- 7. USER ROLES TABLE
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) 
        REFERENCES roles(id) ON DELETE CASCADE
);

-- 8. CUSTOMERS TABLE
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    customer_number VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    date_of_birth DATE NOT NULL,
    national_id VARCHAR(50) UNIQUE NOT NULL,
    passport_number VARCHAR(50),
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    alternate_phone VARCHAR(20),
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(50) NOT NULL,
    occupation VARCHAR(100),
    employer VARCHAR(100),
    annual_income DECIMAL(15,2),
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP,
    verification_remarks VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- 9. CUSTOMER BRANCHES TABLE
CREATE TABLE customer_branches (
    customer_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    PRIMARY KEY (customer_id, branch_id),
    CONSTRAINT fk_cb_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_cb_branch FOREIGN KEY (branch_id)
        REFERENCES branches(id) ON DELETE RESTRICT
);

-- 10. ACCOUNT TYPES TABLE
CREATE TABLE account_types (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(20) UNIQUE NOT NULL,
    type_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    minimum_balance DECIMAL(15,2) DEFAULT 0,
    interest_rate DECIMAL(5,2) DEFAULT 0,
    overdraft_allowed BOOLEAN DEFAULT FALSE,
    daily_withdrawal_limit DECIMAL(15,2),
    monthly_transaction_limit INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- 11. CURRENCIES TABLE
CREATE TABLE currencies (
    id BIGSERIAL PRIMARY KEY,
    currency_code VARCHAR(3) UNIQUE NOT NULL,
    currency_name VARCHAR(50) NOT NULL,
    symbol VARCHAR(5),
    decimal_places INT DEFAULT 2,
    is_base_currency BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. EXCHANGE RATES TABLE
CREATE TABLE exchange_rates (
    id BIGSERIAL PRIMARY KEY,
    base_currency_id BIGINT NOT NULL,
    target_currency_id BIGINT NOT NULL,
    rate DECIMAL(15,6) NOT NULL,
    effective_date DATE NOT NULL,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_er_base_currency FOREIGN KEY (base_currency_id) 
        REFERENCES currencies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_er_target_currency FOREIGN KEY (target_currency_id) 
        REFERENCES currencies(id) ON DELETE RESTRICT,
    CONSTRAINT chk_currencies_different CHECK (base_currency_id != target_currency_id)
);

-- 13. ACCOUNTS TABLE
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    account_type_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    currency_id BIGINT NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    balance DECIMAL(20,2) DEFAULT 0,
    available_balance DECIMAL(20,2) DEFAULT 0,
    interest_rate DECIMAL(5,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_joint_account BOOLEAN DEFAULT FALSE,
    opening_date DATE NOT NULL,
    closing_date DATE,
    last_transaction_date TIMESTAMP,
    overdraft_limit DECIMAL(15,2) DEFAULT 0,
    daily_transaction_limit DECIMAL(15,2),
    maintenance_fee DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_account_type FOREIGN KEY (account_type_id) 
        REFERENCES account_types(id) ON DELETE RESTRICT,
    CONSTRAINT fk_account_branch FOREIGN KEY (branch_id) 
        REFERENCES branches(id) ON DELETE RESTRICT,
    CONSTRAINT fk_account_currency FOREIGN KEY (currency_id) 
        REFERENCES currencies(id) ON DELETE RESTRICT,
    CONSTRAINT chk_account_status CHECK (
        status IN ('ACTIVE', 'DORMANT', 'FROZEN', 'CLOSED')
    ),
    CONSTRAINT chk_account_balance CHECK (balance >= 0)
);

-- 14. ACCOUNT JOINT HOLDERS TABLE
CREATE TABLE account_joint_holders (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    relationship VARCHAR(50),
    is_primary BOOLEAN DEFAULT FALSE,
    signing_authority BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    CONSTRAINT fk_ajh_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_ajh_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT uq_account_customer UNIQUE (account_id, customer_id)
);

-- 15. BENEFICIARIES TABLE
CREATE TABLE beneficiaries (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    beneficiary_account_id BIGINT NOT NULL,
    nickname VARCHAR(50),
    relationship VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_beneficiary_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_beneficiary_account FOREIGN KEY (beneficiary_account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT uq_customer_beneficiary UNIQUE (customer_id, beneficiary_account_id)
);

-- 16. TRANSACTIONS TABLE
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_reference VARCHAR(50) UNIQUE NOT NULL,
    account_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(20,2) NOT NULL,
    balance_before DECIMAL(20,2) NOT NULL,
    balance_after DECIMAL(20,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(200),
    status VARCHAR(20) DEFAULT 'PENDING',
    related_transaction_id BIGINT,
    counterparty_account_id BIGINT,
    counterparty_customer_id BIGINT,
    bank_charges DECIMAL(15,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    currency_id BIGINT NOT NULL,
    exchange_rate DECIMAL(15,6),
    reference_number VARCHAR(50),
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_transaction_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transaction_currency FOREIGN KEY (currency_id) 
        REFERENCES currencies(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transaction_related FOREIGN KEY (related_transaction_id) 
        REFERENCES transactions(id) ON DELETE SET NULL,
    CONSTRAINT chk_transaction_type CHECK (
        transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 
        'FEE', 'INTEREST', 'LOAN_DISBURSEMENT', 'LOAN_REPAYMENT')
    ),
    CONSTRAINT chk_transaction_status CHECK (
        status IN ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED')
    ),
    CONSTRAINT chk_transaction_amount CHECK (amount > 0)
);

-- 17. LOANS TABLE
CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    loan_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    principal_amount DECIMAL(20,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    loan_term_months INT NOT NULL,
    disbursement_date DATE,
    maturity_date DATE,
    monthly_installment DECIMAL(20,2),
    total_interest DECIMAL(20,2),
    total_payable DECIMAL(20,2),
    amount_paid DECIMAL(20,2) DEFAULT 0,
    outstanding_balance DECIMAL(20,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    approved_by BIGINT,
    approval_date TIMESTAMP,
    approved_amount DECIMAL(20,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_loan_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_loan_approved_by FOREIGN KEY (approved_by) 
        REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT chk_loan_status CHECK (
        status IN ('PENDING', 'APPROVED', 'DISBURSED', 'ACTIVE', 'CLOSED', 'DEFAULTED')
    )
);

-- 18. LOAN PAYMENTS TABLE
CREATE TABLE loan_payments (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(20,2) NOT NULL,
    principal_amount DECIMAL(20,2) NOT NULL,
    interest_amount DECIMAL(20,2) NOT NULL,
    penalty_amount DECIMAL(15,2) DEFAULT 0,
    payment_reference VARCHAR(50) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    CONSTRAINT fk_lp_loan FOREIGN KEY (loan_id)
        REFERENCES loans(id) ON DELETE RESTRICT,
    CONSTRAINT fk_lp_transaction FOREIGN KEY (transaction_id)
        REFERENCES transactions(id) ON DELETE RESTRICT
);

-- 19. CARDS TABLE
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(50) NOT NULL,
    account_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    card_brand VARCHAR(20) NOT NULL,
    cardholder_name VARCHAR(100) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv_hash VARCHAR(255) NOT NULL,
    issue_date DATE NOT NULL,
    last_used_date TIMESTAMP,
    daily_limit DECIMAL(15,2),
    monthly_limit DECIMAL(15,2),
    single_transaction_limit DECIMAL(15,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    pin_attempts INT DEFAULT 0,
    blocked_reason VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT fk_card_account FOREIGN KEY(account_id)
        REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_card_customer FOREIGN KEY(customer_id)
        REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT chk_card_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED', 'CANCELLED'))
);

-- 20. AUDIT LOGS TABLE
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    session_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_audit_status CHECK (status IN ('SUCCESS', 'FAILED'))
);

-- 21. LOGIN HISTORY
CREATE TABLE login_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50) NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    session_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    failure_reason VARCHAR(200),
    CONSTRAINT fk_login_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_login_status CHECK (status IN ('SUCCESS', 'FAILED', 'LOGOUT'))
);

-- 22. NOTIFICATIONS TABLE
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    customer_id BIGINT,
    notification_type VARCHAR(50) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL',
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivery_status VARCHAR(20) DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_notification_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT chk_notification_type CHECK (
        notification_type IN ('ACCOUNT_OPENING', 'TRANSACTION', 'LOAN_APPROVAL', 
        'LOAN_REPAYMENT', 'CARD_ISSUE', 'SECURITY_ALERT', 'PROMOTION', 'STATEMENT')
    ),
    CONSTRAINT chk_notification_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH'))
);

-- 23. DAILY TRANSACTION LIMITS TABLE
CREATE TABLE daily_transaction_limits (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    transaction_date DATE NOT NULL,
    total_withdrawals DECIMAL(20,2) DEFAULT 0,
    total_transfers DECIMAL(20,2) DEFAULT 0,
    total_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dtl_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT uq_account_date UNIQUE (account_id, transaction_date)
);

-- 24. SYSTEM CONFIGURATIONS TABLE
CREATE TABLE system_configurations (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    config_group VARCHAR(50),
    description VARCHAR(200),
    data_type VARCHAR(20) DEFAULT 'STRING',
    is_encrypted BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50)
);

-- 25. PASSWORD RESET TOKENS TABLE
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiration_date TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 26. REPORTS TABLE
CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    report_type VARCHAR(50) NOT NULL,
    period_start DATE,
    period_end DATE,
    period_label VARCHAR(100),
    generated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    generated_by VARCHAR(50),
    row_count INT,
    payload TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

---- Indexes

-- Customers
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_national_id ON customers(national_id);
CREATE INDEX idx_customers_name ON customers(first_name, last_name);
CREATE INDEX idx_customers_created_at ON customers(created_at);

-- Accounts
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_account_type ON accounts(account_type_id);
CREATE INDEX idx_accounts_branch ON accounts(branch_id);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_currency ON accounts(currency_id);

-- Transactions
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_reference ON transactions(transaction_reference);
CREATE INDEX idx_transactions_related ON transactions(related_transaction_id);

-- Loans
CREATE INDEX idx_loans_customer_id ON loans(customer_id);
CREATE INDEX idx_loans_account_id ON loans(account_id);
CREATE INDEX idx_loans_status ON loans(status);

-- Cards
CREATE INDEX idx_cards_account_id ON cards(account_id);
CREATE INDEX idx_cards_customer_id ON cards(customer_id);
CREATE INDEX idx_cards_status ON cards(status);

-- Audit
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_action ON audit_logs(action);

-- Login History
CREATE INDEX idx_login_user_id ON login_history(user_id);
CREATE INDEX idx_login_username ON login_history(username);
CREATE INDEX idx_login_time ON login_history(login_time);

-- Security tables
CREATE INDEX idx_login_history_user_id ON login_history(user_id);
CREATE INDEX idx_login_history_username ON login_history(username);
CREATE INDEX idx_login_history_login_time ON login_history(login_time);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);

-- Notifications
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_customer_id ON notifications(customer_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);

-- Beneficiaries
CREATE INDEX idx_beneficiaries_customer ON beneficiaries(customer_id);

-- Reports
CREATE INDEX idx_reports_type ON reports(report_type);
CREATE INDEX idx_reports_generated_on ON reports(generated_on);

-- Insert default Data

-- Currencies
INSERT INTO currencies (currency_code, currency_name, symbol, decimal_places, is_base_currency) VALUES
    ('KES', 'Kenyan Shilling', 'Ksh', 2, TRUE),
    ('USD', 'US Dollar', '$', 2, FALSE);

-- Default Account Types
INSERT INTO account_types (type_code, type_name, description, minimum_balance, interest_rate, overdraft_allowed, daily_withdrawal_limit) VALUES
    ('SAV', 'Savings Account', 'Standard savings account with interest', 500.00, 6.00, FALSE, 40000.00),
    ('CHQ', 'Current Account', 'Current account for daily transactions', 2000.00, 0.00, TRUE, 500000.00),
    ('FIX', 'Fixed Deposit', 'Fixed deposit account with higher interest', 20000.00, 8.00, FALSE, 0.00);

-- Roles
INSERT INTO roles (role_name, role_description, is_system_role) VALUES
    ('ROLE_SUPER_ADMIN', 'Full system administration and emergency access', TRUE),
    ('ROLE_SYSTEM_ADMIN', 'Manages users, roles and system configuration', TRUE),
    ('ROLE_BRANCH_MANAGER', 'Manages branch operations and approvals', TRUE),
    ('ROLE_ASSISTANT_BRANCH_MANAGER', 'Assists the branch manager', TRUE),
    ('ROLE_HEAD_TELLER', 'Supervises tellers and cash operations', TRUE),
    ('ROLE_TELLER', 'Processes customer deposits and withdrawals', TRUE),
    ('ROLE_CUSTOMER_SERVICE_OFFICER', 'Handles customer onboarding and account maintenance', TRUE),
    ('ROLE_LOAN_OFFICER', 'Processes loan applications', TRUE),
    ('ROLE_CREDIT_ANALYST', 'Performs credit analysis and risk assessment', TRUE),
    ('ROLE_LOAN_APPROVER', 'Approves loan applications', TRUE),
    ('ROLE_COLLECTIONS_OFFICER', 'Handles loan collections and recoveries', TRUE),
    ('ROLE_OPERATIONS_OFFICER', 'Performs branch back-office operations', TRUE),
    ('ROLE_OPERATIONS_MANAGER', 'Supervises branch operations', TRUE),
    ('ROLE_CARD_OFFICER', 'Issues, replaces and blocks cards', TRUE),
    ('ROLE_AGENCY_BANKING_OFFICER', 'Manages agency banking operations', TRUE),
    ('ROLE_TREASURY_OFFICER', 'Handles treasury operations', TRUE),
    ('ROLE_FOREX_DEALER', 'Processes foreign exchange transactions', TRUE),
    ('ROLE_COMPLIANCE_OFFICER', 'Ensures regulatory compliance', TRUE),
    ('ROLE_AML_OFFICER', 'Monitors Anti-Money Laundering activities', TRUE),
    ('ROLE_RISK_OFFICER', 'Monitors operational and financial risks', TRUE),
    ('ROLE_AUDITOR', 'Performs internal audits', TRUE),
    ('ROLE_FRAUD_ANALYST', 'Investigates fraudulent activities', TRUE),
    ('ROLE_FINANCE_OFFICER', 'Handles finance and accounting operations', TRUE),
    ('ROLE_CUSTOMER', 'Accesses customer self-service banking', TRUE),
    ('ROLE_DATABASE_ADMIN', 'Administers databases', TRUE),
    ('ROLE_IT_SUPPORT', 'Provides technical support', TRUE),
    ('ROLE_REPORTING_OFFICER', 'Generates regulatory and management reports', TRUE),
    ('ROLE_RELATIONSHIP_MANAGER', 'Manages high-value and corporate clients', TRUE),
    ('ROLE_HR_OFFICER', 'Manages employee records and HR processes', TRUE),
    ('ROLE_EXTERNAL_AUDITOR', 'Read-only access for external audits', TRUE);


-- Insert Permissions

INSERT INTO permissions (permission_name, permission_description, resource_type, action) VALUES

    -- Customer Management
    ('CUSTOMER_CREATE', 'Create new customer', 'CUSTOMER', 'CREATE'),
    ('CUSTOMER_VIEW', 'View customer details', 'CUSTOMER', 'VIEW'),
    ('CUSTOMER_UPDATE', 'Update customer details', 'CUSTOMER', 'UPDATE'),
    ('CUSTOMER_DELETE', 'Delete customer', 'CUSTOMER', 'DELETE'),
    ('CUSTOMER_VERIFY', 'Verify customer identity', 'CUSTOMER', 'VERIFY'),
    ('CUSTOMER_SEARCH', 'Search customers', 'CUSTOMER', 'SEARCH'),
    ('CUSTOMER_EXPORT', 'Export customer information', 'CUSTOMER', 'EXPORT'),
    ('CUSTOMER_REACTIVATE', 'Reactivate customer', 'CUSTOMER', 'REACTIVATE'),
    ('CUSTOMER_DEACTIVATE', 'Deactivate customer', 'CUSTOMER', 'DEACTIVATE'),

    -- Account Management
    ('ACCOUNT_CREATE', 'Open new account', 'ACCOUNT', 'CREATE'),
    ('ACCOUNT_VIEW', 'View account details', 'ACCOUNT', 'VIEW'),
    ('ACCOUNT_UPDATE', 'Update account details', 'ACCOUNT', 'UPDATE'),
    ('ACCOUNT_CLOSE', 'Close account', 'ACCOUNT', 'CLOSE'),
    ('ACCOUNT_FREEZE', 'Freeze account', 'ACCOUNT', 'FREEZE'),
    ('ACCOUNT_SEARCH', 'Search accounts', 'ACCOUNT', 'SEARCH'),
    ('ACCOUNT_REOPEN', 'Reopen closed account', 'ACCOUNT', 'REOPEN'),
    ('ACCOUNT_UNFREEZE', 'Unfreeze account', 'ACCOUNT', 'UNFREEZE'),
    ('ACCOUNT_STATEMENT', 'Generate account statements', 'ACCOUNT', 'STATEMENT'),
    ('ACCOUNT_EXPORT', 'Export account data', 'ACCOUNT', 'EXPORT'),

    -- Transaction Management
    ('TRANSACTION_DEPOSIT', 'Process deposits', 'TRANSACTION', 'DEPOSIT'),
    ('TRANSACTION_WITHDRAW', 'Process withdrawals', 'TRANSACTION', 'WITHDRAW'),
    ('TRANSACTION_TRANSFER', 'Process transfers', 'TRANSACTION', 'TRANSFER'),
    ('TRANSACTION_REVERSE', 'Reverse transactions', 'TRANSACTION', 'REVERSE'),
    ('TRANSACTION_VIEW', 'View transactions', 'TRANSACTION', 'VIEW'),
    ('TRANSACTION_APPROVE', 'Approve transactions', 'TRANSACTION', 'APPROVE'),
    ('TRANSACTION_CANCEL', 'Cancel pending transactions', 'TRANSACTION', 'CANCEL'),
    ('TRANSACTION_EXPORT', 'Export transaction history', 'TRANSACTION', 'EXPORT'),

    -- Loan Management
    ('LOAN_CREATE', 'Create loan application', 'LOAN', 'CREATE'),
    ('LOAN_VIEW', 'View loan details', 'LOAN', 'VIEW'),
    ('LOAN_APPROVE', 'Approve loans', 'LOAN', 'APPROVE'),
    ('LOAN_REPAY', 'Process loan repayment', 'LOAN', 'REPAY'),
    ('LOAN_UPDATE', 'Update loan application', 'LOAN', 'UPDATE'),
    ('LOAN_REJECT', 'Reject loan application', 'LOAN', 'REJECT'),
    ('LOAN_DISBURSE', 'Disburse approved loan', 'LOAN', 'DISBURSE'),
    ('LOAN_CLOSE', 'Close loan', 'LOAN', 'CLOSE'),
    ('LOAN_EXPORT', 'Export loan information', 'LOAN', 'EXPORT'),

    -- Card Management
    ('CARD_ISSUE', 'Issue new card', 'CARD', 'CREATE'),
    ('CARD_BLOCK', 'Block/unblock card', 'CARD', 'BLOCK'),
    ('CARD_VIEW', 'View card details', 'CARD', 'VIEW'),
    ('CARD_REISSUE', 'Reissue bank card', 'CARD', 'REISSUE'),
    ('CARD_REPLACE', 'Replace damaged card', 'CARD', 'REPLACE'),
    ('CARD_UNBLOCK', 'Unblock card', 'CARD', 'UNBLOCK'),
    ('CARD_CLOSE', 'Cancel card permanently', 'CARD', 'CLOSE'),

    -- Beneficiary Management
    ('BENEFICIARY_CREATE', 'Create beneficiary', 'BENEFICIARY', 'CREATE'),
    ('BENEFICIARY_VIEW', 'View beneficiaries', 'BENEFICIARY', 'VIEW'),
    ('BENEFICIARY_UPDATE', 'Update beneficiary', 'BENEFICIARY', 'UPDATE'),
    ('BENEFICIARY_DELETE', 'Delete beneficiary', 'BENEFICIARY', 'DELETE'),

    -- User Management
    ('USER_CREATE', 'Create system users', 'USER', 'CREATE'),
    ('USER_UPDATE', 'Update users', 'USER', 'UPDATE'),
    ('USER_DELETE', 'Delete users', 'USER', 'DELETE'),
    ('USER_VIEW', 'View users', 'USER', 'VIEW'),
    ('USER_LOCK', 'Lock user account', 'USER', 'LOCK'),
    ('USER_UNLOCK', 'Unlock user account', 'USER', 'UNLOCK'),
    ('USER_ENABLE', 'Enable user account', 'USER', 'ENABLE'),
    ('USER_DISABLE', 'Disable user account', 'USER', 'DISABLE'),
    ('USER_RESET_PASSWORD', 'Reset user password', 'USER', 'RESET_PASSWORD'),

    -- Role Management
    ('ROLE_CREATE', 'Create roles', 'ROLE', 'CREATE'),
    ('ROLE_VIEW', 'View roles', 'ROLE', 'VIEW'),
    ('ROLE_UPDATE', 'Update roles', 'ROLE', 'UPDATE'),
    ('ROLE_DELETE', 'Delete roles', 'ROLE', 'DELETE'),
    ('ROLE_ASSIGN_PERMISSION', 'Assign permissions to roles', 'ROLE', 'ASSIGN_PERMISSION'),

    -- Permission Management
    ('PERMISSION_VIEW', 'View permissions', 'PERMISSION', 'VIEW'),
    ('PERMISSION_CREATE', 'Create permissions', 'PERMISSION', 'CREATE'),
    ('PERMISSION_UPDATE', 'Update permissions', 'PERMISSION', 'UPDATE'),

    -- Branch Management
    ('BRANCH_CREATE', 'Create bank branches', 'BRANCH', 'CREATE'),
    ('BRANCH_VIEW', 'View branch information', 'BRANCH', 'VIEW'),
    ('BRANCH_UPDATE', 'Update branch information', 'BRANCH', 'UPDATE'),
    ('BRANCH_DEACTIVATE', 'Deactivate branch', 'BRANCH', 'DEACTIVATE'),

    -- Employee Management
    ('EMPLOYEE_CREATE', 'Create employee records', 'EMPLOYEE', 'CREATE'),
    ('EMPLOYEE_VIEW', 'View employee records', 'EMPLOYEE', 'VIEW'),
    ('EMPLOYEE_UPDATE', 'Update employee records', 'EMPLOYEE', 'UPDATE'),
    ('EMPLOYEE_TERMINATE', 'Terminate employee', 'EMPLOYEE', 'TERMINATE'),
    ('EMPLOYEE_ASSIGN_BRANCH', 'Assign employee to branch', 'EMPLOYEE', 'ASSIGN_BRANCH'),

    -- Notification Management
    ('NOTIFICATION_SEND', 'Send notifications', 'NOTIFICATION', 'SEND'),
    ('NOTIFICATION_VIEW', 'View notifications', 'NOTIFICATION', 'VIEW'),
    ('NOTIFICATION_RESEND', 'Resend failed notifications', 'NOTIFICATION', 'RESEND'),

    -- Exchange Rate Management
    ('EXCHANGE_RATE_VIEW', 'View exchange rates', 'EXCHANGE_RATE', 'VIEW'),
    ('EXCHANGE_RATE_CREATE', 'Create exchange rates', 'EXCHANGE_RATE', 'CREATE'),
    ('EXCHANGE_RATE_UPDATE', 'Update exchange rates', 'EXCHANGE_RATE', 'UPDATE'),
    ('EXCHANGE_RATE_APPROVE', 'Approve exchange rates', 'EXCHANGE_RATE', 'APPROVE'),

    -- Currency Management
    ('CURRENCY_VIEW', 'View currencies', 'CURRENCY', 'VIEW'),
    ('CURRENCY_CREATE', 'Create currencies', 'CURRENCY', 'CREATE'),
    ('CURRENCY_UPDATE', 'Update currencies', 'CURRENCY', 'UPDATE'),

    -- System Configuration
    ('SYSTEM_CONFIG_VIEW', 'View system configuration', 'SYSTEM_CONFIGURATION', 'VIEW'),
    ('SYSTEM_CONFIG_UPDATE', 'Update system configuration', 'SYSTEM_CONFIGURATION', 'UPDATE'),

    -- Audit Management
    ('AUDIT_VIEW', 'View audit logs', 'AUDIT', 'VIEW'),
    ('AUDIT_EXPORT', 'Export audit logs', 'AUDIT', 'EXPORT'),

    -- Login History
    ('LOGIN_HISTORY_VIEW', 'View login history', 'LOGIN_HISTORY', 'VIEW'),
    ('LOGIN_HISTORY_EXPORT', 'Export login history', 'LOGIN_HISTORY', 'EXPORT'),

    -- Reporting
    ('REPORT_VIEW', 'View reports', 'REPORT', 'VIEW'),
    ('REPORT_EXPORT', 'Export reports', 'REPORT', 'EXPORT'),
    ('REPORT_GENERATE', 'Generate reports', 'REPORT', 'GENERATE'),
    ('REPORT_SCHEDULE', 'Schedule reports', 'REPORT', 'SCHEDULE');


---- ASSIGN PERMISSIONS TO ROLES
-- SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_SUPER_ADMIN';

-- SYSTEM_ADMIN gets system management permissions
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_SYSTEM_ADMIN'
AND p.permission_name IN (
    'USER_CREATE', 'USER_VIEW', 'USER_UPDATE', 'USER_DELETE', 'USER_LOCK', 'USER_UNLOCK', 'USER_ENABLE', 'USER_DISABLE', 'USER_RESET_PASSWORD',
    'ROLE_CREATE', 'ROLE_VIEW', 'ROLE_UPDATE', 'ROLE_DELETE', 'ROLE_ASSIGN_PERMISSION',
    'PERMISSION_VIEW', 'PERMISSION_CREATE', 'PERMISSION_UPDATE',
    'SYSTEM_CONFIG_VIEW', 'SYSTEM_CONFIG_UPDATE',
    'AUDIT_VIEW', 'AUDIT_EXPORT',
    'LOGIN_HISTORY_VIEW', 'LOGIN_HISTORY_EXPORT'
);

-- BRANCH_MANAGER gets branch operations permissions
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_BRANCH_MANAGER'
AND p.permission_name IN (
    'CUSTOMER_CREATE', 'CUSTOMER_VIEW', 'CUSTOMER_UPDATE', 'CUSTOMER_VERIFY', 'CUSTOMER_SEARCH', 'CUSTOMER_DEACTIVATE',
    'ACCOUNT_CREATE', 'ACCOUNT_VIEW', 'ACCOUNT_UPDATE', 'ACCOUNT_CLOSE', 'ACCOUNT_FREEZE', 'ACCOUNT_UNFREEZE', 'ACCOUNT_STATEMENT', 'ACCOUNT_SEARCH',
    'TRANSACTION_DEPOSIT', 'TRANSACTION_WITHDRAW', 'TRANSACTION_TRANSFER', 'TRANSACTION_REVERSE', 'TRANSACTION_VIEW', 'TRANSACTION_APPROVE',
    'LOAN_CREATE', 'LOAN_VIEW', 'LOAN_APPROVE', 'LOAN_REPAY', 'LOAN_DISBURSE',
    'CARD_ISSUE', 'CARD_VIEW', 'CARD_BLOCK', 'CARD_UNBLOCK',
    'REPORT_VIEW', 'REPORT_GENERATE', 'REPORT_EXPORT',
    'BRANCH_VIEW',
    'EMPLOYEE_VIEW'
);

-- ASSISTANT_BRANCH_MANAGER gets similar permissions to BRANCH_MANAGER but limited
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_ASSISTANT_BRANCH_MANAGER'
AND p.permission_name IN (
    'CUSTOMER_CREATE', 'CUSTOMER_VIEW', 'CUSTOMER_UPDATE', 'CUSTOMER_VERIFY', 'CUSTOMER_SEARCH',
    'ACCOUNT_CREATE', 'ACCOUNT_VIEW', 'ACCOUNT_UPDATE', 'ACCOUNT_FREEZE', 'ACCOUNT_UNFREEZE', 'ACCOUNT_STATEMENT', 'ACCOUNT_SEARCH',
    'TRANSACTION_DEPOSIT', 'TRANSACTION_WITHDRAW', 'TRANSACTION_TRANSFER', 'TRANSACTION_VIEW', 'TRANSACTION_APPROVE',
    'LOAN_CREATE', 'LOAN_VIEW', 'LOAN_APPROVE', 'LOAN_REPAY',
    'CARD_ISSUE', 'CARD_VIEW', 'CARD_BLOCK', 'CARD_UNBLOCK',
    'REPORT_VIEW', 'REPORT_GENERATE'
);

-- TELLER permissions
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_name IN ('ROLE_TELLER', 'ROLE_HEAD_TELLER')
AND p.permission_name IN (
    'CUSTOMER_CREATE', 'CUSTOMER_VIEW', 'CUSTOMER_UPDATE', 'CUSTOMER_SEARCH',
    'ACCOUNT_CREATE', 'ACCOUNT_VIEW', 'ACCOUNT_STATEMENT', 'ACCOUNT_SEARCH',
    'TRANSACTION_DEPOSIT', 'TRANSACTION_WITHDRAW', 'TRANSACTION_TRANSFER', 'TRANSACTION_VIEW',
    'CARD_VIEW',
    'BENEFICIARY_CREATE', 'BENEFICIARY_VIEW', 'BENEFICIARY_UPDATE'
);

-- LOAN_OFFICER permissions
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_name IN ('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_ANALYST')
AND p.permission_name IN (
    'CUSTOMER_VIEW', 'CUSTOMER_SEARCH',
    'ACCOUNT_VIEW', 'ACCOUNT_SEARCH',
    'LOAN_CREATE', 'LOAN_VIEW', 'LOAN_UPDATE', 'LOAN_REPAY',
    'REPORT_VIEW', 'REPORT_GENERATE'
);

-- LOAN_APPROVER permissions
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_LOAN_APPROVER'
AND p.permission_name IN (
    'CUSTOMER_VIEW', 'CUSTOMER_SEARCH',
    'ACCOUNT_VIEW', 'ACCOUNT_SEARCH',
    'LOAN_VIEW', 'LOAN_APPROVE', 'LOAN_REJECT'
);

-- AUDITOR permissions (read-only)
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_name IN ('ROLE_AUDITOR', 'ROLE_EXTERNAL_AUDITOR')
AND p.permission_name IN (
    'CUSTOMER_VIEW', 'CUSTOMER_SEARCH',
    'ACCOUNT_VIEW', 'ACCOUNT_SEARCH', 'ACCOUNT_STATEMENT',
    'TRANSACTION_VIEW',
    'LOAN_VIEW',
    'CARD_VIEW',
    'AUDIT_VIEW', 'AUDIT_EXPORT',
    'REPORT_VIEW', 'REPORT_GENERATE', 'REPORT_EXPORT'
);

-- COMPLIANCE_OFFICER permissions
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_name IN ('ROLE_COMPLIANCE_OFFICER', 'ROLE_AML_OFFICER')
AND p.permission_name IN (
    'CUSTOMER_VIEW', 'CUSTOMER_SEARCH', 'CUSTOMER_VERIFY',
    'ACCOUNT_VIEW', 'ACCOUNT_SEARCH', 'ACCOUNT_STATEMENT',
    'TRANSACTION_VIEW',
    'LOAN_VIEW',
    'AUDIT_VIEW',
    'REPORT_VIEW', 'REPORT_GENERATE', 'REPORT_EXPORT'
);

-- CUSTOMER permissions
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_CUSTOMER'
AND p.permission_name IN (
    'CUSTOMER_VIEW',
    'ACCOUNT_VIEW', 'ACCOUNT_STATEMENT',
    'TRANSACTION_VIEW',
    'BENEFICIARY_CREATE', 'BENEFICIARY_VIEW', 'BENEFICIARY_UPDATE', 'BENEFICIARY_DELETE'
);


-- TABLE Comments

COMMENT ON TABLE branches IS 
'Stores bank branch information including location, contact details, management and operational status.';

COMMENT ON TABLE employees IS 
'Stores employee records, branch assignments, employment details and staff status.';

COMMENT ON TABLE permissions IS 
'Defines individual system permissions used for role-based access control(RBAC).';

COMMENT ON TABLE roles IS 
'Defines roles that group permissions for users.';

COMMENT ON TABLE role_permissions IS 
'Maps permissions to roles, defining the actions each role is authorized to perform.';

COMMENT ON TABLE users IS 
'Stores system user accounts, authentication information and login status.';

COMMENT ON TABLE user_roles IS 
'Associates system users with one or more application roles.';

COMMENT ON TABLE customers IS 
'Stores customer personal information, contact details, KYC information and verification status.';

COMMENT ON TABLE customer_branches IS 
'Associates customers with the branch responsible for managing their banking relationship.';

COMMENT ON TABLE account_types IS 
'Defines available banking account products and their operating rules, limits and interest rates.';

COMMENT ON TABLE currencies IS 
'Stores currencies supported by the banking system.';

COMMENT ON TABLE exchange_rates IS 
'Stores exchange rates between supported currencies with effective and expiry dates.';

COMMENT ON TABLE accounts IS 
'Stores customer deposit accounts, balances, account type, currency and branch.';

COMMENT ON TABLE account_joint_holders IS 
'Stores additional account holders and signing authorities for joint accounts.';

COMMENT ON TABLE beneficiaries IS 
'Stores customer-approved beneficiary accounts for transfers and payments.';

COMMENT ON TABLE transactions IS 
'Stores all financial transactions including deposits, withdrawals, transfers, fees, interest and loan-related transactions.';

COMMENT ON TABLE loans IS 
'Stores customer loan applications, approvals, disbursements, repayment information and loan status.';

COMMENT ON TABLE loan_payments IS 
'Stores repayments made against customer loans including principal, interest, penalties and linked transactions.';

COMMENT ON TABLE cards IS 
'Stores debit, credit and prepaid cards issued to customers and linked to their accounts.';

COMMENT ON TABLE audit_logs IS 
'Stores security and audit events for user actions, data changes and system activities.';

COMMENT ON TABLE login_history IS 
'Stores user authentication history including successful logins, failed login attempts, logout events and session information.';

COMMENT ON TABLE notifications IS 
'Stores notifications generated by the system for users and customers through supported communication channels.';

COMMENT ON TABLE daily_transaction_limits IS 
'Tracks daily account transaction totals used to enforce withdrawal and transfer limits.';

COMMENT ON TABLE system_configurations IS 
'Stores configurable system-wide settings, operational parameters and application configuration values.';

-- END OF SCHEMA