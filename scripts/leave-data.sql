-- =====================================================
-- Leave Requests
-- =====================================================
DROP TABLE IF EXISTS leave_requests;

CREATE TABLE leave_requests
(
    id               UUID PRIMARY KEY,
    employee_id      UUID         NOT NULL,
    manager_id       UUID         NOT NULL,
    leave_type       VARCHAR(50)  NOT NULL,
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    number_of_days   INTEGER      NOT NULL,
    reason           VARCHAR(500) NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    rejection_reason VARCHAR(1000),
    comments         VARCHAR(1000),
    applied_at       TIMESTAMP,
    updated_at       TIMESTAMP
);