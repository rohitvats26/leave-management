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
