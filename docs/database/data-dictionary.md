# Digital Library Management System - Data Dictionary

This document details the schema definitions, column types, nullability, constraints, and descriptions for all tables.

---

## 1. Table: `libraries`
Represents physical library facilities.

| Column | Data Type | Nullable | Default | Description |
|---|---|:---:|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary key |
| `name` | `VARCHAR(150)` | No | - | Name of the library |
| `address` | `TEXT` | No | - | Physical location address |
| `contact_phone` | `VARCHAR(20)` | No | - | Helpline phone number |
| `opening_time` | `TIME` | No | `06:00:00` | Daily opening hours |
| `closing_time` | `TIME` | No | `23:00:00` | Daily closing hours |
| `total_seats` | `INTEGER` | No | `50` | Total seat capacity |
| `created_at` | `TIMESTAMPTZ` | No | `now()` | Record creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | Yes | - | Last modification timestamp |

---

## 2. Table: `library_settings`
Configurable business policies per library branch.

| Column | Data Type | Nullable | Default | Description |
|---|---|:---:|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary key |
| `library_id` | `BIGINT` | No | - | Foreign key to `libraries(id)` |
| `monthly_fee_amount` | `NUMERIC(10,2)` | No | `700.00` | Standard monthly subscription fee |
| `grace_period_days` | `INTEGER` | No | `7` | Days before check-in is blocked |
| `attendance_block_after_days` | `INTEGER` | No | `8` | Day check-in is strictly blocked |
| `seat_release_after_days` | `INTEGER` | No | `15` | Day seat becomes eligible for release |
| `reservation_timeout_minutes`| `INTEGER` | No | `10` | Temporary seat hold expiration |
| `allow_qr_attendance` | `BOOLEAN` | No | `true` | Enables rotating QR check-in |

---

## 3. Table: `users`
Accounts for students, admins, and attendants.

| Column | Data Type | Nullable | Default | Description |
|---|---|:---:|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary key |
| `library_id` | `BIGINT` | No | - | Foreign key to `libraries(id)` |
| `phone_number` | `VARCHAR(20)` | No | - | Unique phone login identifier |
| `email` | `VARCHAR(150)` | Yes | - | Optional email address |
| `password_hash`| `VARCHAR(255)` | No | - | BCrypt hashed password |
| `full_name` | `VARCHAR(150)` | No | - | Student or staff full name |
| `role` | `VARCHAR(30)` | No | - | `ROLE_STUDENT`, `ROLE_ADMIN`, `ROLE_STAFF` |
| `status` | `VARCHAR(30)` | No | `'ACTIVE'`| `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `created_at` | `TIMESTAMPTZ` | No | `now()` | Creation timestamp |

---

## 4. Table: `seats`
Individual study desks (A01–A50).

| Column | Data Type | Nullable | Default | Description |
|---|---|:---:|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary key |
| `library_id` | `BIGINT` | No | - | Foreign key to `libraries(id)` |
| `seat_number` | `VARCHAR(20)` | No | - | Human-readable label (e.g., "A23") |
| `row_number` | `INTEGER` | No | `1` | Grid row layout coordinate |
| `col_number` | `INTEGER` | No | `1` | Grid column layout coordinate |
| `status` | `VARCHAR(30)` | No | `'AVAILABLE'`| `AVAILABLE`, `RESERVED`, `OCCUPIED`, `MAINTENANCE` |
| `version` | `BIGINT` | No | `0` | Optimistic locking counter |

---

## 5. Table: `admissions`
Formal student admission linking student to seat and joining date.

| Column | Data Type | Nullable | Default | Description |
|---|---|:---:|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary key |
| `library_id` | `BIGINT` | No | - | Foreign key to `libraries(id)` |
| `student_id` | `BIGINT` | No | - | Foreign key to `users(id)` |
| `seat_id` | `BIGINT` | No | - | Foreign key to `seats(id)` |
| `joining_date` | `DATE` | No | - | Anchor date for monthly billing |
| `emergency_contact`| `VARCHAR(20)`| Yes| - | Relative/guardian phone number |
| `status` | `VARCHAR(30)` | No | `'ACTIVE'`| `ACTIVE`, `CANCELLED`, `COMPLETED` |
| `created_at` | `TIMESTAMPTZ` | No | `now()` | Admission timestamp |

---

## 6. Table: `subscriptions`
Billing cycles anchored to student joining date.

| Column | Data Type | Nullable | Default | Description |
|---|---|:---:|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary key |
| `library_id` | `BIGINT` | No | - | Foreign key to `libraries(id)` |
| `student_id` | `BIGINT` | No | - | Foreign key to `users(id)` |
| `start_date` | `DATE` | No | - | Cycle start date (e.g., 2026-08-15) |
| `end_date` | `DATE` | No | - | Cycle end date (e.g., 2026-09-14) |
| `due_date` | `DATE` | No | - | Due date for next renewal (2026-09-15) |
| `grace_until` | `DATE` | Yes | - | Extended grace cutoff if granted |
| `amount` | `NUMERIC(10,2)` | No | `700.00` | Subscription amount |
| `status` | `VARCHAR(30)` | No | `'ACTIVE'`| `ACTIVE`, `PAYMENT_DUE`, `OVERDUE`, `ATTENDANCE_BLOCKED`, `EXTENDED`, `ELIGIBLE_FOR_RELEASE` |
| `version` | `BIGINT` | No | `0` | Optimistic locking version |

---

## 7. Table: `payments`
Financial transactions for online Razorpay and offline cash payments.

| Column | Data Type | Nullable | Default | Description |
|---|---|:---:|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary key |
| `library_id` | `BIGINT` | No | - | Foreign key to `libraries(id)` |
| `student_id` | `BIGINT` | No | - | Foreign key to `users(id)` |
| `subscription_id` | `BIGINT` | Yes | - | Foreign key to `subscriptions(id)` |
| `amount` | `NUMERIC(10,2)` | No | - | Paid amount in INR (e.g., 700.00) |
| `method` | `VARCHAR(30)` | No | - | `ONLINE_RAZORPAY`, `CASH` |
| `status` | `VARCHAR(30)` | No | - | `PENDING`, `SUCCESS`, `FAILED`, `REFUNDED` |
| `gateway_order_id` | `VARCHAR(100)`| Yes| - | Razorpay Order ID (`order_...`) |
| `gateway_payment_id`| `VARCHAR(100)`| Yes| - | Razorpay Payment ID (`pay_...`, unique) |
| `idempotency_key` | `VARCHAR(100)`| Yes| - | Client idempotency UUID |
| `recorded_by` | `VARCHAR(100)`| Yes| - | Admin ID who logged cash payment |
| `payment_date` | `TIMESTAMPTZ` | No | `now()` | Transaction timestamp |

---

## 8. Table: `attendance_records`
Daily check-in and check-out logs.

| Column | Data Type | Nullable | Default | Description |
|---|---|:---:|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary key |
| `library_id` | `BIGINT` | No | - | Foreign key to `libraries(id)` |
| `student_id` | `BIGINT` | No | - | Foreign key to `users(id)` |
| `seat_id` | `BIGINT` | No | - | Foreign key to `seats(id)` |
| `date` | `DATE` | No | - | Session date (YYYY-MM-DD) |
| `check_in_time`| `TIMESTAMPTZ` | No | - | In-time timestamp |
| `check_out_time`| `TIMESTAMPTZ`| Yes| - | Out-time timestamp |
| `duration_minutes`| `INTEGER` | Yes| - | Computed study duration in minutes |
| `status` | `VARCHAR(30)` | No | `'PRESENT'`| `PRESENT`, `CHECKED_OUT`, `INCOMPLETE` |
| `verification_method`| `VARCHAR(30)`| No | `'QR_ROTATING'`| `QR_ROTATING`, `MANUAL_ADMIN` |

---

## 9. Table: `audit_logs`
Immutable record of sensitive owner and system actions.

| Column | Data Type | Nullable | Default | Description |
|---|---|:---:|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary key |
| `library_id` | `BIGINT` | No | - | Foreign key to `libraries(id)` |
| `actor_id` | `BIGINT` | No | - | User ID of person making modification |
| `action` | `VARCHAR(100)` | No | - | E.g., `GRANT_EXTENSION`, `SEAT_SWAP`, `CASH_PAYMENT_RECORDED` |
| `entity_type` | `VARCHAR(50)` | No | - | `SUBSCRIPTION`, `SEAT`, `PAYMENT`, `ATTENDANCE` |
| `entity_id` | `BIGINT` | No | - | ID of modified entity |
| `old_value` | `TEXT` | Yes | - | JSON snapshot prior to edit |
| `new_value` | `TEXT` | Yes | - | JSON snapshot after edit |
| `ip_address` | `VARCHAR(45)` | Yes | - | Client IP address |
| `timestamp` | `TIMESTAMPTZ` | No | `now()` | Log creation timestamp |
