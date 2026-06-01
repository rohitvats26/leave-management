-- Passwords are BCrypt of "Password@123"

INSERT INTO users (id, username, password, email, role, enabled, created_at) VALUES
  ('550e8400-e29b-41d4-a716-446655440001', 'john.smith',
   '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W',
   'john.smith@company.com',   'ROLE_EMPLOYEE', true, CURRENT_TIMESTAMP),

  ('550e8400-e29b-41d4-a716-446655440002', 'priya.sharma',
   '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W',
   'priya.sharma@company.com', 'ROLE_EMPLOYEE', true, CURRENT_TIMESTAMP),

  ('550e8400-e29b-41d4-a716-446655440010', 'rahul.verma',
   '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W',
   'rahul.verma@company.com',  'ROLE_MANAGER',  true, CURRENT_TIMESTAMP),

  ('550e8400-e29b-41d4-a716-446655440011', 'anita.gupta',
   '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W',
   'anita.gupta@company.com',  'ROLE_MANAGER',  true, CURRENT_TIMESTAMP);