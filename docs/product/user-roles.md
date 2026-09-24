# Digital Library Management System - User Roles & Permissions

## 1. Overview
The application enforces strict **Role-Based Access Control (RBAC)** across the Flutter mobile interface and Spring Boot REST API endpoints. Permissions are verified server-side on every request via JWT claims and Spring Security annotations (`@PreAuthorize`).

---

## 2. Defined Roles

### 1. `ROLE_STUDENT`
Standard user registered for self-study access.
- **Seat Access:** Can view full library layout and seat statuses; can reserve and book available seats; cannot modify seat status directly.
- **Profile:** Can view and update own profile (contact info, emergency phone, photo); cannot view other students' profiles.
- **Attendance:** Can scan dynamic QR codes to check in and check out; can view own historical attendance logs and statistics.
- **Billing & Payments:** Can view own active subscription cycle, due date, payment history, and receipts; can initiate online Razorpay payment for renewals; cannot record cash payments.
- **Helpline:** Can initiate phone call to library owner via device dialer.

### 2. `ROLE_ADMIN` (Library Owner)
Owner and administrator of the physical library branch.
- **Seat Management:** Can view, edit, configure, release, and place any seat into maintenance.
- **Student Management:** Can view all student profiles, admissions, contact details, and attendance history; can block or reactivate students.
- **Attendance Control:** Can view live headcount inside library, view daily logs, manually check students in/out, and correct attendance discrepancies with audit notes.
- **Financial Control:** Can view revenue analytics, fees collected, pending balances; can record offline cash payments; can grant payment grace extensions.
- **Audit Logs:** Read-only access to complete audit trail of administrative modifications.
- **Library Settings:** Can configure monthly fees, grace period days, check-in block thresholds, seat release durations, and library operating hours.

### 3. `ROLE_STAFF` (Assistant / Desk Attendant - Future Ready)
Floor attendant or evening shift helper.
- **Permissions:** Can view seat layout, view live headcount, assist with QR scanning, and record manual check-ins. Cannot access financial reports or delete admissions.

### 4. `ROLE_SUPER_ADMIN` (Platform Administrator - SaaS Ready)
System operator managing multiple library accounts.
- **Permissions:** Can create library tenants, manage tenant subscriptions, and view system health metrics across all libraries.

---

## 3. Role-to-Feature Permission Matrix

| Feature / Action | STUDENT | ADMIN / OWNER | STAFF | SUPER_ADMIN |
|---|:---:|:---:|:---:|:---:|
| View Seat Layout | Yes | Yes | Yes | Yes |
| Reserve / Book Seat | Yes (Self) | Yes (Any) | Yes (Any) | Yes |
| Put Seat in Maintenance | No | Yes | No | Yes |
| Release Occupied Seat | No | Yes | No | Yes |
| View Own Attendance | Yes | Yes | Yes | Yes |
| View All Attendance | No | Yes | Yes | Yes |
| Scan QR to Check In | Yes (Self) | Yes | Yes | Yes |
| Manual Attendance Override | No | Yes | Yes | Yes |
| View Own Subscription | Yes | Yes | Yes | Yes |
| Initiate Online Payment | Yes (Self) | Yes | No | Yes |
| Record Cash Payment | No | Yes | Yes (Optional) | Yes |
| Grant Due Date Extension | No | Yes | No | Yes |
| View Financial Reports | No | Yes | No | Yes |
| Edit Library Settings | No | Yes | No | Yes |
| Access Audit Logs | No | Yes (Branch) | No | Yes (All) |
| Manage Multi-Tenants | No | No | No | Yes |
