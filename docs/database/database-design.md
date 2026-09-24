# Digital Library Management System - Database Design

## 1. Design Principles & Conventions

1. **Relational Integrity First:** Every relationship between entities has foreign keys with explicit cascade or restrict constraints.
2. **Multi-Tenant Ready (`library_id`):** Every operational table (`seats`, `admissions`, `subscriptions`, `payments`, `attendance_records`, `notifications`, `audit_logs`) has a mandatory `library_id` foreign key.
3. **Idempotency & Concurrency:**
   - Unique constraints on critical transaction references: `payments(gateway_payment_id)`, `payments(idempotency_key)`.
   - Unique constraints on business unicity: `seats(library_id, seat_number)`, `admissions(library_id, student_id, status='ACTIVE')`.
   - Optimistic locking version field `version` on `seats` and `subscriptions`.
4. **Auditability:** Every entity extends an auditable base with `created_at`, `updated_at`, and `created_by`. Sensitive administrative actions are separately recorded in an append-only `audit_logs` table.
5. **Timezone Standardization:** All timestamps stored as `TIMESTAMP WITH TIME ZONE` in UTC.

---

## 2. Table Indexing Strategy

- `idx_seats_library_status`: `seats(library_id, status)` - Optimizes fast visual seat layout queries.
- `idx_subscriptions_due_status`: `subscriptions(due_date, status)` - Optimizes daily scheduled billing cron jobs.
- `idx_attendance_student_date`: `attendance_records(student_id, date)` - Optimizes student history queries and duplicate check-in checks.
- `idx_payments_student`: `payments(student_id, created_at DESC)` - Optimizes receipt history fetching.
- `idx_audit_logs_entity`: `audit_logs(entity_type, entity_id)` - Fast lookup of past actions on any student, seat, or payment.

---

## 3. Flyway Migration Versioning

Database migrations are managed via Flyway located in `backend/src/main/resources/db/migration/`:
- `V1__init_schema.sql` - Core schema: libraries, settings, users, roles, seats.
- `V2__admissions_and_subscriptions.sql` - Admissions, subscriptions, reservations.
- `V3__payments_and_attendance.sql` - Payments, attendance records, rotating QR tokens.
- `V4__notifications_and_audit.sql` - Notification logs, audit trails.
- `V5__seed_default_library.sql` - Initial seed data: 1 library, 50 seats (A01-A50), default admin user, default library settings.
