-- Passwords are BCrypt of "Password@123"
INSERT INTO users (id, username, password, email, role, enabled, created_at) VALUES
  (RANDOM_UUID(), 'john.smith',   '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W', 'john.smith@company.com',   'ROLE_EMPLOYEE', true, CURRENT_TIMESTAMP),
  (RANDOM_UUID(), 'priya.sharma', '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W', 'priya.sharma@company.com', 'ROLE_EMPLOYEE', true, CURRENT_TIMESTAMP),
  (RANDOM_UUID(), 'rahul.verma', '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W', 'rahul.verma@company.com',  'ROLE_MANAGER',  true, CURRENT_TIMESTAMP),
  (RANDOM_UUID(), 'anita.gupta', '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W', 'anita.gupta@company.com',  'ROLE_MANAGER',  true, CURRENT_TIMESTAMP);
