-- =====================================================
-- Employees
-- =====================================================
DROP TABLE IF EXISTS employees;

CREATE TABLE employees
(
    id         UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    username   VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(100),
    manager_id UUID,
    role       VARCHAR(50)  NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- Leave Balances
-- =====================================================

DROP TABLE IF EXISTS leave_balances;

CREATE TABLE IF NOT EXISTS leave_balances
(
    id          UUID PRIMARY KEY,
    employee_id UUID        NOT NULL,
    leave_type  VARCHAR(20) NOT NULL,
    allocated   INTEGER     NOT NULL DEFAULT 0,
    used        INTEGER     NOT NULL DEFAULT 0,

    CONSTRAINT uk_employee_leave_type
    UNIQUE (employee_id, leave_type)
    );


-- ── Employees ────────────────────────────────────────────────────
INSERT INTO employees (id, first_name, last_name, email, username, department, manager_id, role, enabled, created_at) VALUES
  ('550e8400-e29b-41d4-a716-446655440001',
   'John', 'Smith', 'john.smith@company.com', 'john.smith',
   'Engineering', '550e8400-e29b-41d4-a716-446655440010',
   'ROLE_EMPLOYEE', true, CURRENT_TIMESTAMP),

  ('550e8400-e29b-41d4-a716-446655440002',
   'Priya', 'Sharma', 'priya.sharma@company.com', 'priya.sharma',
   'Engineering', '550e8400-e29b-41d4-a716-446655440010',
   'ROLE_EMPLOYEE', true, CURRENT_TIMESTAMP),

  ('550e8400-e29b-41d4-a716-446655440010',
   'Rahul', 'Verma', 'rahul.verma@company.com', 'rahul.verma',
   'Engineering', NULL,
   'ROLE_MANAGER', true, CURRENT_TIMESTAMP),

  ('550e8400-e29b-41d4-a716-446655440011',
   'Anita', 'Gupta', 'anita.gupta@company.com', 'anita.gupta',
   'HR', NULL,
   'ROLE_MANAGER', true, CURRENT_TIMESTAMP);

-- ── Leave Balances — john.smith ──────────────────────────────────
-- 1 APPROVED casual leave (3 days) already taken → used=3, remaining=9
INSERT INTO leave_balances (id, employee_id, leave_type, allocated, used) VALUES
  ('aa000001-0000-0000-0000-000000000001',
   '550e8400-e29b-41d4-a716-446655440001', 'CASUAL',    12, 0),
  ('aa000001-0000-0000-0000-000000000002',
   '550e8400-e29b-41d4-a716-446655440001', 'SICK',      10, 0),
  ('aa000001-0000-0000-0000-000000000003',
   '550e8400-e29b-41d4-a716-446655440001', 'PRIVILEGE', 15, 0);

-- ── Leave Balances — priya.sharma ────────────────────────────────
-- 1 APPROVED sick leave (2 days) already taken → used=2, remaining=8
INSERT INTO leave_balances (id, employee_id, leave_type, allocated, used) VALUES
  ('aa000002-0000-0000-0000-000000000001',
   '550e8400-e29b-41d4-a716-446655440002', 'CASUAL',    12, 0),
  ('aa000002-0000-0000-0000-000000000002',
   '550e8400-e29b-41d4-a716-446655440002', 'SICK',      10, 0),
  ('aa000002-0000-0000-0000-000000000003',
   '550e8400-e29b-41d4-a716-446655440002', 'PRIVILEGE', 15, 0);
