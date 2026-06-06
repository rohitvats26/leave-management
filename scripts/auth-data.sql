-- =====================================================
-- Users
-- =====================================================

DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    role       VARCHAR(50)  NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- Seed Data
-- Password: Password@123
-- =====================================================

INSERT INTO users (id, username, password, email, role, enabled, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'john.smith',
        '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W',
        'john.smith@company.com', 'ROLE_EMPLOYEE', true, CURRENT_TIMESTAMP),

       ('550e8400-e29b-41d4-a716-446655440002', 'priya.sharma',
        '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W',
        'priya.sharma@company.com', 'ROLE_EMPLOYEE', true, CURRENT_TIMESTAMP),

       ('550e8400-e29b-41d4-a716-446655440010', 'rahul.verma',
        '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W',
        'rahul.verma@company.com', 'ROLE_MANAGER', true, CURRENT_TIMESTAMP),

       ('550e8400-e29b-41d4-a716-446655440011', 'anita.gupta',
        '$2a$12$qNf8CM1QJRqa3pi.TyoIKeod8Pz49oq0vY8b1NoGHbkiDH53.KO.W',
        'anita.gupta@company.com', 'ROLE_MANAGER', true, CURRENT_TIMESTAMP);