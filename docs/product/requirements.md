# Digital Library Management System - Requirements Specification

## 1. Functional Requirements (FR)

### Module 1: Authentication & User Accounts
- **FR-AUTH-01:** Students and administrators can securely register and log in using phone number / email and password.
- **FR-AUTH-02:** System issues short-lived JWT access tokens (e.g., 60 minutes) and persistent refresh tokens (30 days).
- **FR-AUTH-03:** Passwords must be hashed using BCrypt (work factor 12+).
- **FR-AUTH-04:** Mobile app persists tokens securely in `flutter_secure_storage` (Android Keystore / iOS Keychain).
- **FR-AUTH-05:** Automatic token refresh interceptor handles expired access tokens seamlessly.

### Module 2: Seat Management & Visual Layout
- **FR-SEAT-01:** Visual representation of physical seats (A01–A50) mapped in an interactive 2D grid.
- **FR-SEAT-02:** Real-time seat status indicators:
  - `AVAILABLE` (Green)
  - `OCCUPIED` (Red)
  - `RESERVED` (Yellow - temporary reservation during checkout)
  - `MAINTENANCE` (Grey - out of service)
- **FR-SEAT-03:** Concurrency protection: Pessimistic/optimistic locking prevents simultaneous booking of the same seat by multiple users.
- **FR-SEAT-04:** Temporary reservation hold expires automatically after 10 minutes if payment/admission is abandoned.

### Module 3: Student Admission Flow
- **FR-ADM-01:** Prospective students can browse available seats, select an open desk, and fill in necessary admission details (Full Name, Phone, Email, Emergency Contact, Shift/Plan).
- **FR-ADM-02:** Admission checkout links chosen seat to subscription plan and initiates Razorpay payment.
- **FR-ADM-03:** Upon verified payment, student profile is activated, seat is permanently marked `OCCUPIED`, and subscription is initialized.
- **FR-ADM-04:** Administrator receives real-time notification with student details and direct "Call Student" action.

### Module 4: Attendance System
- **FR-ATT-01:** Daily check-in and checkout recorded with date, timestamps, duration, and seat number.
- **FR-ATT-02:** Rotating QR Code Security: Attendance validation uses dynamically generated, time-limited server tokens (TTL 30 seconds) to prevent photo replay fraud.
- **FR-ATT-03:** Duplicate check-in prevention: A student cannot check in twice on the same day without an intervening checkout.
- **FR-ATT-04:** Overdue check-in blocking: Students whose subscription is past the grace period (`ATTENDANCE_BLOCKED`) are barred from check-in unless an admin extension is active.
- **FR-ATT-05:** Administrative correction: Owner can manually log or amend attendance with audit reason.

### Module 5: Subscription & Billing Lifecycle
- **FR-SUB-01:** Individual billing cycles: Due dates calculated strictly based on joining date (e.g., Joined Aug 15 -> Due Sept 15).
- **FR-SUB-02:** Dynamic lifecycle states:
  - `ACTIVE`: Current cycle paid.
  - `PAYMENT_DUE`: Due date arrived.
  - `OVERDUE`: Past due date, within grace period (Days 1–7).
  - `ATTENDANCE_BLOCKED`: Past grace period (Day 8+), check-in denied.
  - `EXTENDED`: Manually extended by admin.
  - `ELIGIBLE_FOR_RELEASE`: Past release threshold (Day 15+), seat eligible for reallocation.
  - `RELEASED`: Seat vacated and marked available.
- **FR-SUB-03:** Configurable thresholds stored per library: Grace period days (default 7), block days (default 8), release days (default 15).

### Module 6: Payment Processing (Razorpay & Cash)
- **FR-PAY-01:** Online payment order generation via Razorpay API (UPI, Netbanking, Cards, Wallets).
- **FR-PAY-02:** Server-side HMAC-SHA256 signature verification of `razorpay_order_id`, `razorpay_payment_id`, and `razorpay_signature`.
- **FR-PAY-03:** Razorpay webhook handling for asynchronous reconciliation (e.g., app dropped network after payment).
- **FR-PAY-04:** Idempotent payment processing: Webhooks and verification endpoints reject duplicate transaction IDs.
- **FR-PAY-05:** Admin Cash Payment recording: Owner records offline cash receipts, automatically advancing the subscription cycle.
- **FR-PAY-06:** In-app digital receipt history for both online and cash payments.

### Module 7: Notifications & Reminders
- **FR-NOTIF-01:** Automated fee reminder scheduler sends alerts (e.g., 5 days before, 2 days before, on due date, overdue warnings).
- **FR-NOTIF-02:** In-app notification center tracks message history and read/unread flags.
- **FR-NOTIF-03:** Push notification delivery via Firebase Cloud Messaging (FCM).

### Module 8: Owner Operational Dashboard & Overrides
- **FR-ADM-01:** High-level metrics: Total seats, Occupancy rate, Live inside headcount, Fees collected, Pending arrears, Expiring subscriptions.
- **FR-ADM-02:** Action-required feed: Overdue students, pending reservations, unverified payments.
- **FR-ADM-03:** Administrative actions: Grant extension, swap seat, release seat, block/unblock student, correct attendance.
- **FR-ADM-04:** Immutable audit logging: Captures actor, action, target entity, timestamp, before/after snapshot.

---

## 2. Non-Functional Requirements (NFR)

- **NFR-SEC-01 (Security):** All API traffic over HTTPS; sensitive tokens stored in hardware-backed storage; zero credentials in Git.
- **NFR-PERF-01 (Performance):** Seat layout and dashboard queries respond in < 150ms for 50–500 seats.
- **NFR-REL-01 (Reliability):** Database transactions guarantee ACID compliance for payments, admissions, and seat bookings.
- **NFR-AVAIL-01 (Availability):** 99.9% uptime target on cost-effective VPS containerized deployment.
- **NFR-UX-01 (User Experience):** Responsive mobile interface with clear visual hierarchy, large touch targets (min 48dp), high contrast, and dark mode support.
- **NFR-SCALE-01 (Extensibility):** Multi-library partitioning (`library_id`) enables expansion into SaaS without schema rewrites.
