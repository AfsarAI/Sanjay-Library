-- ==============================================================================
-- DIGITAL LIBRARY MANAGEMENT SYSTEM - V1 INITIAL RELATIONAL SCHEMA
-- ==============================================================================

-- 1. Libraries Table
CREATE TABLE libraries (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    opening_time TIME NOT NULL DEFAULT '06:00:00',
    closing_time TIME NOT NULL DEFAULT '23:00:00',
    total_seats INT NOT NULL DEFAULT 50,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

-- 2. Library Settings Table
CREATE TABLE library_settings (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL UNIQUE REFERENCES libraries(id) ON DELETE CASCADE,
    monthly_fee_amount NUMERIC(10,2) NOT NULL DEFAULT 700.00,
    grace_period_days INT NOT NULL DEFAULT 7,
    attendance_block_after_days INT NOT NULL DEFAULT 8,
    seat_release_after_days INT NOT NULL DEFAULT 15,
    reservation_timeout_minutes INT NOT NULL DEFAULT 10,
    allow_qr_attendance BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

-- 3. Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES libraries(id) ON DELETE RESTRICT,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_STUDENT',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT uq_users_phone UNIQUE (phone_number),
    CONSTRAINT chk_users_role CHECK (role IN ('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_SUPER_ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);
CREATE INDEX idx_users_library_role ON users(library_id, role);

-- 4. Refresh Tokens Table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    device_info VARCHAR(255),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- 5. Seats Table
CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    seat_number VARCHAR(20) NOT NULL,
    row_number INT NOT NULL DEFAULT 1,
    col_number INT NOT NULL DEFAULT 1,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT uq_library_seat UNIQUE (library_id, seat_number),
    CONSTRAINT chk_seat_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE'))
);
CREATE INDEX idx_seats_library_status ON seats(library_id, status);

-- 6. Seat Reservations Table (Temporary 10-minute locking during booking)
CREATE TABLE seat_reservations (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    seat_id BIGINT NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_reservation_status CHECK (status IN ('PENDING', 'CONFIRMED', 'EXPIRED', 'CANCELLED'))
);
CREATE INDEX idx_seat_reservations_seat_status ON seat_reservations(seat_id, status);
CREATE INDEX idx_seat_reservations_expiry ON seat_reservations(expires_at) WHERE status = 'PENDING';

-- 7. Admissions Table
CREATE TABLE admissions (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES libraries(id) ON DELETE RESTRICT,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    seat_id BIGINT NOT NULL REFERENCES seats(id) ON DELETE RESTRICT,
    joining_date DATE NOT NULL,
    emergency_contact VARCHAR(20),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT chk_admission_status CHECK (status IN ('ACTIVE', 'CANCELLED', 'COMPLETED'))
);
CREATE INDEX idx_admissions_student ON admissions(student_id);
CREATE INDEX idx_admissions_seat ON admissions(seat_id);

-- 8. Subscriptions Table (Individual cycle anchored to joining date)
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES libraries(id) ON DELETE RESTRICT,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    due_date DATE NOT NULL,
    grace_until DATE,
    amount NUMERIC(10,2) NOT NULL DEFAULT 700.00,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT chk_subscription_status CHECK (status IN (
        'ACTIVE', 'PAYMENT_DUE', 'OVERDUE', 'ATTENDANCE_BLOCKED', 'EXTENDED', 'ELIGIBLE_FOR_RELEASE', 'RELEASED'
    ))
);
CREATE INDEX idx_subscriptions_student ON subscriptions(student_id);
CREATE INDEX idx_subscriptions_due_status ON subscriptions(due_date, status);

-- 9. Payments Table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES libraries(id) ON DELETE RESTRICT,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    subscription_id BIGINT REFERENCES subscriptions(id) ON DELETE SET NULL,
    amount NUMERIC(10,2) NOT NULL,
    method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    gateway_order_id VARCHAR(100),
    gateway_payment_id VARCHAR(100) UNIQUE,
    idempotency_key VARCHAR(100) UNIQUE,
    recorded_by VARCHAR(100),
    notes TEXT,
    payment_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_payment_method CHECK (method IN ('ONLINE_RAZORPAY', 'CASH')),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'))
);
CREATE INDEX idx_payments_student ON payments(student_id, created_at DESC);
CREATE INDEX idx_payments_order ON payments(gateway_order_id);

-- 10. Attendance Records Table
CREATE TABLE attendance_records (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES libraries(id) ON DELETE RESTRICT,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    seat_id BIGINT NOT NULL REFERENCES seats(id) ON DELETE RESTRICT,
    date DATE NOT NULL,
    check_in_time TIMESTAMPTZ NOT NULL,
    check_out_time TIMESTAMPTZ,
    duration_minutes INT,
    status VARCHAR(30) NOT NULL DEFAULT 'PRESENT',
    verification_method VARCHAR(30) NOT NULL DEFAULT 'QR_ROTATING',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_student_daily_attendance UNIQUE (student_id, date),
    CONSTRAINT chk_attendance_status CHECK (status IN ('PRESENT', 'CHECKED_OUT', 'INCOMPLETE')),
    CONSTRAINT chk_attendance_method CHECK (verification_method IN ('QR_ROTATING', 'MANUAL_ADMIN'))
);
CREATE INDEX idx_attendance_library_date ON attendance_records(library_id, date);
CREATE INDEX idx_attendance_student_date ON attendance_records(student_id, date DESC);

-- 11. Rotating QR Attendance Tokens Table
CREATE TABLE attendance_qr_tokens (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    nonce VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_qr_tokens_validity ON attendance_qr_tokens(expires_at);

-- 12. Notifications Table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    channel VARCHAR(30) NOT NULL DEFAULT 'IN_APP',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read);

-- 13. Audit Logs Table (Append-only)
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES libraries(id) ON DELETE RESTRICT,
    actor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_library_timestamp ON audit_logs(library_id, timestamp DESC);
