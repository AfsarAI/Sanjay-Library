# Digital Library Management System - Business Rules

This document specifies the exact domain logic and rules governing seat allocation, joining-date-based billing cycles, attendance eligibility, and administrative overrides.

---

## 1. Seat Allocation & Concurrency Rules

### Rule 1.1: Single Student to Single Active Seat
- A student may occupy at most ONE active seat at any given time within a library branch.
- A seat can only have one of four valid states: `AVAILABLE`, `RESERVED`, `OCCUPIED`, `MAINTENANCE`.

### Rule 1.2: Reservation Timeout Policy
- When a student initiates booking, the seat transitions from `AVAILABLE` to `RESERVED` with a 10-minute validity (`expires_at = now() + 10 minutes`).
- If payment/admission verification does not complete within 10 minutes, a background cleanup job resets the seat state to `AVAILABLE`.
- No two prospective students can reserve the same seat simultaneously. The database enforces this via row-level pessimistic locking (`SELECT ... FOR UPDATE`) during reservation creation.

---

## 2. Joining-Date-Based Billing Cycles

### Rule 2.1: Individualized Billing Anchor
- Subscriptions are strictly calculated relative to the student's **Joining Date**.
- Calendar-month billing (1st of month) is NOT assumed.
- **Example:**
  - Joining Date: August 15th
  - Cycle 1: 15 August to 14 September (Due: 15 September)
  - Cycle 2: 15 September to 14 October (Due: 15 October)
- **Leap Year & Month-End Handling:**
  - If a student joins on January 31st, next due date in February is February 28th (or 29th in leap years) using standard Java `LocalDate.plusMonths(1)` semantics.

---

## 3. Overdue & Lifecycle Progression Rules

The subscription lifecycle progresses based on configurable parameters in `library_settings`:

| Setting Key | Default Value | Description |
|---|---|---|
| `monthly_fee_amount` | ₹700 | Base monthly fee per student seat |
| `grace_period_days` | 7 days | Permitted grace window after due date before blocking attendance |
| `attendance_block_after_days` | 8 days | Day from due date when attendance is strictly denied |
| `seat_release_after_days` | 15 days | Day from due date when seat becomes eligible for automatic or manual release |
| `reservation_timeout_minutes`| 10 minutes | Maximum duration a temporary seat lock is held |

### Lifecycle Timeline:
```
Day 0: Due Date
  └── Status: PAYMENT_DUE
      └── Automated reminder sent to student.

Days 1 to 7: Grace Period
  └── Status: OVERDUE
      └── Student attendance remains permitted.
      └── Daily reminder sent (Day 2, Day 5, Day 7).

Day 8: Block Threshold
  └── Status: ATTENDANCE_BLOCKED
      └── Student mobile check-in is strictly blocked with code OVERDUE_ATTENDANCE_BLOCKED.
      └── Notification sent: "Attendance temporarily suspended due to unpaid fee."

Days 8 to 14: Extension Window
  └── Owner may grant an explicit administrative extension (`grace_until = new_date`, status `EXTENDED`).
  └── If extended, attendance is re-enabled until the new date.

Day 15+: Release Threshold
  └── Status: ELIGIBLE_FOR_RELEASE
      └── Seat can be released by admin or automatically reassigned.
      └── Student notified of seat forfeiture.
```

---

## 4. Attendance Rules

### Rule 4.1: Daily Session Validity
- A student may check in only once per active session.
- Check-out completes the session and computes total duration in minutes.
- Check-out without an active check-in is rejected with an error.

### Rule 4.2: Anti-Fraud QR Security
- The physical QR code displayed in the library refreshes every 30 seconds.
- QR content is a cryptographically signed HMAC token containing `{library_id, timestamp, nonce}`.
- Replaying a photographed QR code after 30 seconds results in an invalid token rejection.

### Rule 4.3: Attendance Eligibility Pre-Check
Before recording a check-in, the system evaluates:
1. Is the student's status `ACTIVE`? (If suspended, reject).
2. Is the subscription status `ACTIVE`, `PAYMENT_DUE`, `OVERDUE` (within grace), or `EXTENDED`? (If `ATTENDANCE_BLOCKED` or `EXPIRED`, reject).
3. Is the assigned seat currently marked `OCCUPIED`?

---

## 5. Payment & Idempotency Rules

### Rule 5.1: Zero Trust in Client Payment Assertions
- The mobile application's claim of payment success is never trusted.
- Payment is only considered final when verified via Razorpay HMAC-SHA256 signature verification or authenticated server-to-server webhook.

### Rule 5.2: Strict Webhook Idempotency
- Every payment transaction is keyed by `gateway_payment_id` with a database `UNIQUE` constraint.
- If a webhook payload is received multiple times for the same transaction, the backend acknowledges with HTTP 200 without re-extending the subscription.

### Rule 5.3: Cash Payment Exclusivity
- Only authenticated users with `ROLE_ADMIN` or `ROLE_STAFF` can record cash payments.
- Cash payments must record the receiving staff member's ID and mandatory audit log entry.
