# Digital Library Management System - User Flows

This document details the primary user journeys and interaction flows across the mobile client and backend.

---

## 1. Student New Admission Flow

```mermaid
sequenceDiagram
    autonumber
    actor Student
    participant Mobile as Flutter App
    participant Backend as Spring Boot API
    participant PG as PostgreSQL
    participant RZP as Razorpay Gateway
    actor Owner as Library Owner

    Student->>Mobile: Open App & Tap "New Admission"
    Mobile->>Backend: GET /api/v1/seats/layout
    Backend->>PG: Query seats & current status
    PG-->>Backend: Return seats (A01-A50)
    Backend-->>Mobile: Display visual seat map
    Student->>Mobile: Selects Seat A23
    Mobile->>Backend: POST /api/v1/seats/A23/reserve (temporary lock)
    Backend->>PG: BEGIN; SELECT ... FOR UPDATE; Create reservation (10 min TTL); COMMIT
    Backend-->>Mobile: Reservation Confirmed (Seat A23 locked)
    Student->>Mobile: Fills profile info (Name, Phone, Emergency Contact, Plan)
    Student->>Mobile: Taps "Pay & Confirm Admission"
    Mobile->>Backend: POST /api/v1/payments/create-order
    Backend->>RZP: Create Order (Amount, Receipt ID)
    RZP-->>Backend: Return razorpay_order_id
    Backend-->>Mobile: Send Order Details
    Mobile->>RZP: Open Razorpay Native Checkout (UPI / Cards)
    Student->>RZP: Authorizes payment
    RZP-->>Mobile: Returns payment_id & signature
    Mobile->>Backend: POST /api/v1/payments/verify
    Backend->>Backend: Verify HMAC-SHA256 signature
    Backend->>PG: Transition Seat A23 -> OCCUPIED; Create Admission; Initialize Subscription; Record Payment
    Backend-->>Mobile: Admission Successful!
    Backend->>Owner: Push Notification: "New Student Rahul admitted to Seat A23"
```

---

## 2. Daily Attendance Flow (Rotating QR Security)

```mermaid
sequenceDiagram
    autonumber
    actor Student
    participant Mobile as Flutter App
    participant Screen as In-Library Tablet/Display
    participant Backend as Spring Boot API
    participant PG as PostgreSQL

    Note over Screen,Backend: Tablet requests new rotating QR every 30s
    Backend-->>Screen: Display QR with signed JWT Token (TTL 30s)
    Student->>Mobile: Opens App & Taps "Check In"
    Mobile->>Mobile: Opens camera scanner
    Student->>Screen: Scans rotating QR Code
    Mobile->>Backend: POST /api/v1/attendance/check-in {qr_token}
    Backend->>Backend: 1. Validate QR Token signature & expiry
    Backend->>PG: 2. Check student subscription status & grace period
    alt Subscription is OVERDUE & past grace period (ATTENDANCE_BLOCKED)
        Backend-->>Mobile: 403 Forbidden: "Attendance blocked due to overdue fee. Contact owner."
    else Active or Grace Valid
        Backend->>PG: 3. Verify no open check-in today; Insert Attendance Record
        Backend-->>Mobile: 200 OK: "Checked in successfully at 08:15 AM! Seat A23"
    end
```

---

## 3. Subscription Renewal Flow (Online & Cash)

### Online Renewal (Student-Initiated)
1. Student receives push reminder: "Your monthly library fee of ₹700 is due on 15 Oct".
2. Student opens dashboard -> Taps **[Pay Now ₹700]**.
3. Mobile calls `/api/v1/payments/create-order`.
4. Razorpay checkout opens on device -> Student approves UPI intent (GPay/PhonePe).
5. Razorpay verifies payment -> Mobile sends signature to `/api/v1/payments/verify`.
6. Concurrently, Razorpay Webhook fires to `/api/v1/payments/webhook`.
7. Backend ensures idempotency:
   - Validates signature.
   - Advances subscription cycle by 1 month (`due_date = due_date + 1 month`).
   - Resets status to `ACTIVE`.
8. Student receives instant digital confirmation and updated receipt.

### Offline Cash Renewal (Admin-Initiated)
1. Student hands ₹700 cash to the library owner.
2. Owner opens Admin Mobile App -> Searches for student "Rahul Kumar".
3. Owner taps **[Record Cash Payment]** -> Inputs ₹700, date, and optional receipt remark.
4. Mobile calls `POST /api/v1/admin/payments/cash`.
5. Backend writes payment record (`method = CASH`, `recorded_by = owner_id`), extends subscription by 1 month, and writes an audit log.
6. Student receives push notification: "Cash payment of ₹700 recorded by Library Owner. Subscription extended to 15 Nov."

---

## 4. Admin Extension & Seat Release Flow

1. Student reaches Day 8 without payment -> System automatically transitions subscription to `ATTENDANCE_BLOCKED`.
2. Student contacts owner requesting 4 days of extra time.
3. Owner opens Admin Mobile App -> Taps **[Grant Extension]** -> Selects new date (e.g., +4 days) -> Enters reason: *"Student requested exam extension"*.
4. Backend updates `subscription.grace_until`, sets status to `EXTENDED`, and unblocks attendance.
5. Action is recorded in `audit_logs`.
6. If student does not pay by the extension date and reaches Day 15 overdue:
   - Background job flags student as `ELIGIBLE_FOR_RELEASE`.
   - Owner receives notification: *"Seat A23 eligible for release"*.
   - Owner taps **[Release Seat]** -> Seat becomes `AVAILABLE` for new admissions.
