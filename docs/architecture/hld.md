# Digital Library Management System - High-Level Design (HLD)

## 1. System Context Diagram (C4 Level 1)

```mermaid
C4Context
    title System Context for Digital Library Management System

    Person(student, "Library Student", "Uses assigned seat, pays monthly subscription, records daily attendance.")
    Person(owner, "Library Owner / Admin", "Manages seats, monitors attendance, handles billing overrides, views revenue.")
    
    System(librarySystem, "Digital Library System", "Centralized platform managing seats, admissions, attendance, payments, and subscriptions.")
    
    System_Ext(razorpay, "Razorpay Payment Gateway", "Processes UPI, card, and netbanking payments; dispatches webhooks.")
    System_Ext(fcm, "Firebase Cloud Messaging", "Delivers push notifications to mobile devices.")

    Rel(student, librarySystem, "Selects seat, checks in/out, pays fees, views status", "HTTPS / Flutter App")
    Rel(owner, librarySystem, "Oversees occupancy, grants extensions, records cash, adjusts settings", "HTTPS / Flutter App")
    Rel(librarySystem, razorpay, "Creates payment orders, verifies signatures", "HTTPS REST")
    Rel(razorpay, librarySystem, "Sends webhook events (payment.captured)", "HTTPS Webhook")
    Rel(librarySystem, fcm, "Dispatches push alerts", "HTTP v1 API")
    Rel(fcm, student, "Delivers fee reminders and confirmation alerts", "Mobile Push")
    Rel(fcm, owner, "Delivers new admission and release alerts", "Mobile Push")
```

---

## 2. Container Diagram (C4 Level 2)

```mermaid
C4Container
    title Container Diagram for Digital Library Management System

    Person(student, "Student", "Self-study library member")
    Person(owner, "Library Owner", "Administrator")

    Container(flutterApp, "Flutter Mobile Application", "Dart, Flutter, Riverpod, GoRouter, Dio", "Cross-platform mobile client providing student and admin dashboards, seat map, QR scanner, and Razorpay checkout.")
    
    Container(springBoot, "Spring Boot Backend", "Java 21/25, Spring Boot 3.3.x, Spring Security, Hibernate", "Modular monolith exposing RESTful APIs, executing business rules, scheduled billing crons, and audit logging.")
    
    ContainerDb(postgresDb, "PostgreSQL Database", "PostgreSQL 16", "Stores relational data: libraries, seats, users, admissions, subscriptions, payments, attendance records, audit logs.")
    
    System_Ext(razorpay, "Razorpay API", "Payment Processor")
    System_Ext(fcm, "Firebase Cloud Messaging", "Push Notification Service")

    Rel(student, flutterApp, "Interacts with", "Mobile UI")
    Rel(owner, flutterApp, "Interacts with", "Mobile UI")
    Rel(flutterApp, springBoot, "Invokes APIs", "JSON/HTTPS via Port 443/8080")
    Rel(springBoot, postgresDb, "Reads and writes data", "JDBC / Port 5432")
    Rel(springBoot, razorpay, "Creates orders and verifies webhooks", "HTTPS")
    Rel(springBoot, fcm, "Sends push notifications", "HTTPS")
```

---

## 3. Core Component Interactions

### 3.1 Concurrency & Seat Locking Flow
```mermaid
sequenceDiagram
    autonumber
    actor User1 as Student 1
    actor User2 as Student 2
    participant API as Spring Boot API
    participant DB as PostgreSQL (Seats Table)

    User1->>API: POST /api/v1/seats/A15/reserve
    User2->>API: POST /api/v1/seats/A15/reserve
    Note over API,DB: API handles User1 first inside @Transactional
    API->>DB: SELECT * FROM seats WHERE seat_number = 'A15' FOR UPDATE
    DB-->>API: Seat status = AVAILABLE
    API->>DB: UPDATE seats SET status = 'RESERVED' WHERE id = 15; INSERT INTO seat_reservations (expires_at = now() + 10m)
    API-->>User1: 200 OK: Seat A15 reserved for 10 minutes
    Note over API,DB: API now processes User2
    API->>DB: SELECT * FROM seats WHERE seat_number = 'A15' FOR UPDATE
    DB-->>API: Seat status = RESERVED
    API-->>User2: 409 Conflict: "Seat A15 is currently reserved. Please choose another seat."
```

### 3.2 Scheduled Billing & Lifecycle Engine
```mermaid
sequenceDiagram
    autonumber
    participant Cron as Spring Scheduler (@Scheduled)
    participant SubService as SubscriptionService
    participant DB as PostgreSQL
    participant NotifService as NotificationService

    Note over Cron: Runs daily at 00:05 AM IST
    Cron->>SubService: processDailyBillingCycle()
    SubService->>DB: Query subscriptions where due_date <= TODAY and status = 'ACTIVE'
    DB-->>SubService: Return due subscriptions
    SubService->>DB: UPDATE status = 'PAYMENT_DUE'
    SubService->>NotifService: Dispatch "Payment Due Today" alerts

    SubService->>DB: Query subscriptions where due_date + grace_period_days < TODAY and status = 'OVERDUE'
    DB-->>SubService: Return overdue subscriptions past grace
    SubService->>DB: UPDATE status = 'ATTENDANCE_BLOCKED'
    SubService->>NotifService: Dispatch "Attendance Suspended" alerts

    SubService->>DB: Query subscriptions where due_date + seat_release_after_days < TODAY and status = 'ATTENDANCE_BLOCKED'
    DB-->>SubService: Return eligible release candidates
    SubService->>DB: UPDATE status = 'ELIGIBLE_FOR_RELEASE'
    SubService->>NotifService: Alert Owner: "Seat X eligible for release"
```
