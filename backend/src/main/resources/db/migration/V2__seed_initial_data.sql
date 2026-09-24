-- ==============================================================================
-- DIGITAL LIBRARY MANAGEMENT SYSTEM - V2 SEED DATA
-- ==============================================================================

-- 1. Insert Default Library
INSERT INTO libraries (id, name, address, contact_phone, opening_time, closing_time, total_seats, created_at)
VALUES (
    1,
    'Apex Self-Study Digital Library',
    'Main Market Road, Near City Center, Civil Lines',
    '+91-9876543210',
    '06:00:00',
    '23:00:00',
    50,
    now()
);

-- 2. Insert Default Library Settings
INSERT INTO library_settings (id, library_id, monthly_fee_amount, grace_period_days, attendance_block_after_days, seat_release_after_days, reservation_timeout_minutes, allow_qr_attendance, created_at)
VALUES (
    1,
    1,
    700.00,
    7,
    8,
    15,
    10,
    true,
    now()
);

-- 3. Insert Initial Seats (A01 to A50) in a 5x10 layout
INSERT INTO seats (library_id, seat_number, row_number, col_number, status, version, created_at) VALUES
(1, 'A01', 1, 1, 'AVAILABLE', 0, now()),
(1, 'A02', 1, 2, 'AVAILABLE', 0, now()),
(1, 'A03', 1, 3, 'AVAILABLE', 0, now()),
(1, 'A04', 1, 4, 'AVAILABLE', 0, now()),
(1, 'A05', 1, 5, 'AVAILABLE', 0, now()),
(1, 'A06', 1, 6, 'AVAILABLE', 0, now()),
(1, 'A07', 1, 7, 'AVAILABLE', 0, now()),
(1, 'A08', 1, 8, 'AVAILABLE', 0, now()),
(1, 'A09', 1, 9, 'AVAILABLE', 0, now()),
(1, 'A10', 1, 10, 'AVAILABLE', 0, now()),

(1, 'A11', 2, 1, 'AVAILABLE', 0, now()),
(1, 'A12', 2, 2, 'AVAILABLE', 0, now()),
(1, 'A13', 2, 3, 'AVAILABLE', 0, now()),
(1, 'A14', 2, 4, 'AVAILABLE', 0, now()),
(1, 'A15', 2, 5, 'AVAILABLE', 0, now()),
(1, 'A16', 2, 6, 'AVAILABLE', 0, now()),
(1, 'A17', 2, 7, 'AVAILABLE', 0, now()),
(1, 'A18', 2, 8, 'AVAILABLE', 0, now()),
(1, 'A19', 2, 9, 'AVAILABLE', 0, now()),
(1, 'A20', 2, 10, 'AVAILABLE', 0, now()),

(1, 'A21', 3, 1, 'AVAILABLE', 0, now()),
(1, 'A22', 3, 2, 'AVAILABLE', 0, now()),
(1, 'A23', 3, 3, 'AVAILABLE', 0, now()),
(1, 'A24', 3, 4, 'AVAILABLE', 0, now()),
(1, 'A25', 3, 5, 'AVAILABLE', 0, now()),
(1, 'A26', 3, 6, 'AVAILABLE', 0, now()),
(1, 'A27', 3, 7, 'AVAILABLE', 0, now()),
(1, 'A28', 3, 8, 'AVAILABLE', 0, now()),
(1, 'A29', 3, 9, 'AVAILABLE', 0, now()),
(1, 'A30', 3, 10, 'AVAILABLE', 0, now()),

(1, 'A31', 4, 1, 'AVAILABLE', 0, now()),
(1, 'A32', 4, 2, 'AVAILABLE', 0, now()),
(1, 'A33', 4, 3, 'AVAILABLE', 0, now()),
(1, 'A34', 4, 4, 'AVAILABLE', 0, now()),
(1, 'A35', 4, 5, 'AVAILABLE', 0, now()),
(1, 'A36', 4, 6, 'AVAILABLE', 0, now()),
(1, 'A37', 4, 7, 'AVAILABLE', 0, now()),
(1, 'A38', 4, 8, 'AVAILABLE', 0, now()),
(1, 'A39', 4, 9, 'AVAILABLE', 0, now()),
(1, 'A40', 4, 10, 'AVAILABLE', 0, now()),

(1, 'A41', 5, 1, 'AVAILABLE', 0, now()),
(1, 'A42', 5, 2, 'AVAILABLE', 0, now()),
(1, 'A43', 5, 3, 'AVAILABLE', 0, now()),
(1, 'A44', 5, 4, 'AVAILABLE', 0, now()),
(1, 'A45', 5, 5, 'AVAILABLE', 0, now()),
(1, 'A46', 5, 6, 'AVAILABLE', 0, now()),
(1, 'A47', 5, 7, 'AVAILABLE', 0, now()),
(1, 'A48', 5, 8, 'AVAILABLE', 0, now()),
(1, 'A49', 5, 9, 'AVAILABLE', 0, now()),
(1, 'A50', 5, 10, 'AVAILABLE', 0, now());

-- Reset sequence for tables
SELECT setval('libraries_id_seq', (SELECT MAX(id) FROM libraries));
SELECT setval('library_settings_id_seq', (SELECT MAX(id) FROM library_settings));
SELECT setval('seats_id_seq', (SELECT MAX(id) FROM seats));
