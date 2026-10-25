-- ============================================
-- V2: SEED DEFAULT ADMIN USER
-- ============================================
-- Creates a default branch, employee and admin user so the system can be accessed for the first time.
--
-- Login:     admin
-- Password:  Admin@123
-- Role:      ROLE_SUPER_ADMIN (all permissions)
-- ============================================

-- 1. Default branch (Head Office)
INSERT INTO branches (branch_code, branch_name, address_line1, city, country, phone, email, is_active)
SELECT 'HQ001', 'Head Office', 'Westlands', 'Nairobi', 'Kenya', '+254711223344', 'hq@banking.com', TRUE
WHERE NOT EXISTS (SELECT 1 FROM branches WHERE branch_code = 'HQ001');

-- 2. Default employee (System Administrator)
INSERT INTO employees (
    employee_no, first_name, middle_name, last_name, email, phone,
    branch_id, department, position, hire_date, is_active
)
SELECT 'EMP001', 'System', NULL, 'Administrator', 'admin@banking.com', '+254711111111', b.id, 'IT', 'System Administrator', DATE '2024-01-01', TRUE
FROM branches b
WHERE b.branch_code = 'HQ001'
  AND NOT EXISTS (SELECT 1 FROM employees WHERE employee_no = 'EMP001');

-- 3. Admin user
-- The BCrypt hash below corresponds to plaintext password: Admin@123
-- (BCrypt strength 12; generated via new BCryptPasswordEncoder(12).encode("Admin@123"))
INSERT INTO users (
    username, password_hash, employee_id,
    is_account_locked, is_credentials_expired, is_enabled,
    failed_login_attempts, password_changed_at
)
SELECT 'admin', '$2a$12$KA3QJgMmsvbN2PLs2apG2.xvasMOh2ZjTPocmOlTlT3fdz.jXMWi6', e.id, FALSE, FALSE, TRUE, 0, CURRENT_TIMESTAMP
FROM employees e
WHERE e.employee_no = 'EMP001'
  AND NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- 4. Assign ROLE_SUPER_ADMIN to admin user
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, 'SYSTEM'
FROM users u, roles r
WHERE u.username = 'admin'
  AND r.role_name = 'ROLE_SUPER_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- 5. Also assign ROLE_SYSTEM_ADMIN for good measure
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, 'SYSTEM'
FROM users u, roles r
WHERE u.username = 'admin'
  AND r.role_name = 'ROLE_SYSTEM_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );