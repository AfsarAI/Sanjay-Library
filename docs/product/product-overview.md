# Sanjay Library - Product Overview
### Digital Library Management System for Sanjay Library

## 1. Executive Summary
**Sanjay Library** is a production-grade, mobile-first software application engineered for Sanjay (Library Owner) and his self-study digital library operating in an urban and semi-urban Indian environment. In Sanjay Library, students pay a recurring monthly subscription fee to secure a designated, quiet study desk with power, high-speed Wi-Fi, and study amenities.

Prior to this software, Sanjay Library operated using manual paper registers for daily attendance, handwritten cash receipts or direct personal UPI transfers to Sanjay, manual desk allocation charts, and ad-hoc phone calls for fee collection reminders.

This application transitions Sanjay Library into a fully digitized, automated, secure, and auditable operation.

---

## 2. Core Value Propositions

### For Students
- **Real-Time Seat Booking:** Visual, interactive layout map displaying exactly which seats (A01–A50) are free, occupied, or reserved.
- **Transparent Billing:** Personalized subscription cycles calculated strictly from their joining date (e.g., 15th to 14th of each month), showing active validity, due dates, and payment history.
- **Convenient In-App UPI & Card Payments:** Native integration with Razorpay supporting Google Pay, PhonePe, Paytm, cards, and netbanking, providing instant receipts and subscription renewals.
- **Frictionless Daily Attendance:** Fast check-in and checkout via rotating in-library QR codes or authenticated one-touch attendance.
- **Direct Owner Assistance:** In-app one-touch calling to the library owner for quick queries or emergency support.

### For the Library Owner / Administrator
- **Centralized Operational Dashboard:** Real-time metrics on occupancy (Occupied, Free, Reserved, Maintenance), live head-count currently inside the library, fees collected, pending dues, and new admissions.
- **Automated Billing Lifecycle:** Automatic status progression from `ACTIVE` to `PAYMENT_DUE`, `OVERDUE`, `ATTENDANCE_BLOCKED`, and `ELIGIBLE_FOR_RELEASE`.
- **Administrative Overrides with Audit Trail:** Power to grant payment grace extensions, adjust seat assignments, record cash payments, and correct attendance with mandatory immutable audit logging.
- **Revenue Protection:** Prevents unpaid students from occupying seats indefinitely or checking in after the overdue grace period expires.
- **Cash Transaction Ledger:** Official digital recording of cash payments with receipts accessible on the student's mobile app.

---

## 3. Physical Library Context & Initial Scope
- **Capacity:** ~50 physical study desks/seats (e.g., Desks A01 to A50), with potential expansion to 100–200 seats.
- **User Base:** 100+ active registered students, with concurrent morning, afternoon, evening, or full-day study shifts.
- **Initial Geography:** India (IST timezone, INR ₹ currency, UPI payment preference).
- **Architecture Standard:** Production-grade modular monolith, clean code architecture, multi-tenant capable schema to support future multi-branch/SaaS operations.
