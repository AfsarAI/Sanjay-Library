# Sanjay Library
### Digital Library Management System for Sanjay Library

[![CI Pipeline](https://github.com/AfsarAI/Sanjay-Library/actions/workflows/ci.yml/badge.svg)](https://github.com/AfsarAI/Sanjay-Library/actions/workflows/ci.yml)
[![Flutter](https://img.shields.io/badge/Flutter-3.47+-02569B?logo=flutter&logoColor=white)](https://flutter.dev)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21+-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com)

---

## 1. Project Overview
**Sanjay Library** is a production-grade, mobile-first software solution designed specifically for Sanjay (Library Owner) and the students of his physical self-study library. It replaces physical paper registers, manual UPI reminders, and cash ledgers with a centralized, secure digital system: **Digital Library Management System for Sanjay Library**.

### Core Value Deliverables:
- **For Students**: Mobile app to inspect real-time 50 physical desks (A01–A50), reserve a seat with temporary lock, apply for admission, check in/out daily using dynamic rotating QR codes, renew subscriptions via Razorpay, and view past attendance logs.
- **For the Owner/Admin**: Central control dashboard providing live headcount of students studying inside, real-time desk occupancy metrics, automated fee overdue tracking, one-click grace extensions, and offline cash fee recording.

---

## 2. Tech Stack

| Layer | Technology | Key Libraries / Modules |
| :--- | :--- | :--- |
| **Mobile Client** | Flutter 3.47+ / Dart 3.13+ | Riverpod 2, Dio, GoRouter, flutter_secure_storage, mobile_scanner, GoogleFonts |
| **Backend API** | Java 21+ / Spring Boot 3.3.4 | Spring Web, Spring Security, Spring Data JPA, Flyway, Bean Validation, Spring Scheduler |
| **Database** | PostgreSQL 16 | Relational schema with Flyway migrations V1 & V2, indexes, and pessimistic write locks |
| **Security** | Stateless JWT | Access Tokens (1h), Refresh Tokens (30d), PBKDF2 / BCrypt password hashing |
| **Payments** | Razorpay + Cash Ledger | Server-side HMAC-SHA256 signature verification and idempotent processing |
| **DevOps / CI** | Docker, Compose, GitHub Actions | Multi-stage Docker builds, PostgreSQL containerization, CI test automation |

---

## 3. High-Level Architecture

```
                    ┌────────────────────────────────────────┐
                    │    Flutter Mobile App (iOS / Android)  │
                    │   • Interactive 2D Seat Map (A01-A50)  │
                    │   • Dynamic QR Code Scanner View       │
                    │   • Joining-Date Subscription Card     │
                    │   • Razorpay Online Payment Checkout   │
                    │   • Owner Real-Time Admin Command      │
                    └───────────────────┬────────────────────┘
                                        │ HTTPS / REST (JSON)
                                        │ Bearer JWT Authentication
                                        ▼
                    ┌────────────────────────────────────────┐
                    │       Spring Boot Modular Monolith     │
                    │   • Auth & Role-Based Access Control   │
                    │   • Pessimistic Lock Seat Reservation  │
                    │   • Admission & Enrollment Service     │
                    │   • Cryptographic Rotating QR Service  │
                    │   • Joining-Date Anchor Billing Engine │
                    │   • Razorpay Signature Verification    │
                    │   • Scheduled Background Jobs          │
                    │   • Immutable Audit Logging            │
                    └───────────────────┬────────────────────┘
                                        │ JDBC (HikariCP)
                                        │ Flyway Versioned Migrations
                                        ▼
                    ┌────────────────────────────────────────┐
                    │         PostgreSQL 16 Database         │
                    │   • 13 Normalized Relational Tables    │
                    │   • Multi-Tenant Ready (`library_id`)  │
                    │   • Foreign Keys, Indexes, Constraints │
                    └────────────────────────────────────────┘
```

---

## 4. Key Business Rules & Workflows

### A. 2D Seat Reservation Concurrency
- When a student taps an available desk, `SeatService.reserveSeat()` uses a **Pessimistic Write Lock** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) inside a `READ_COMMITTED` transaction.
- Desk is locked with a temporary 10-minute hold (`SeatReservation`).
- A background cron (`ScheduledReservationCleanupJob`) runs every 60 seconds to release unfulfilled holds back to `AVAILABLE`.

### B. Joining-Date Monthly Billing Cycle
- In self-study libraries, students join on various days of the month.
- The subscription cycle is strictly anchored to the student's admission date (e.g. 15 Aug to 14 Sep, next due 15 Sep).
- Built-in lifecycle policy:
  - **Days 1–7 (Grace Period)**: Student allowed entry with payment reminder notifications.
  - **Day 8+ (Overdue Block)**: Daily attendance check-in is **automatically blocked**.
  - **Day 15+ (Seat Release)**: Seat becomes eligible for automated release if no extension is granted.

### C. Admin Overrides & Cash Ledger
- The owner can grant an administrative grace period extension, instantly unblocking attendance with an immutable audit log entry.
- The owner can record offline cash payments directly from the Admin dashboard, automatically extending the student's subscription cycle by 1 month.

### D. Dynamic Rotating QR Attendance
- To eliminate spoofing from static QR screenshots, attendance utilizes a 30-second cryptographic rotating token generated by the server.
- The mobile scanner detects the token and verifies both token validity and subscription standing before recording arrival.

---

## 5. Local Setup & Quick Start

### Prerequisites
- **Java 21+ JDK** installed (`java -version`)
- **Maven 3.9+** (`mvn -v`)
- **Flutter 3.27+** (`flutter --version`)
- **Docker & Docker Compose** (`docker compose version`)

### 1. Clone the Repository
```bash
git clone https://github.com/AfsarAI/Sanjay-Library.git
cd Sanjay-Library
```

### 2. Launch PostgreSQL with Docker
```bash
docker compose up -d postgres
```
*PostgreSQL container `digital_library_postgres` will start on port 5432.*

### 3. Run Backend API
```bash
cd backend
mvn spring-boot:run
```
*Flyway will automatically execute migrations `V1__init_schema.sql` and `V2__seed_initial_data.sql` and seed default test credentials.*

- **Health Endpoint**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **Interactive Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI v3 Docs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 4. Seeded Demo Accounts

| Role | Mobile Phone | Password | Capabilities |
| :--- | :--- | :--- | :--- |
| **Library Owner / Admin** | `9876543210` | `Admin@123` | Occupancy meters, seat release, cash payment logging, grace extensions |
| **Demo Student** | `9123456780` | `Student@123` | Active desk view, QR check-in, subscription renewal, attendance logs |

### 5. Run Flutter Mobile App
```bash
cd mobile
flutter pub get
flutter run
```
*The app includes convenient one-tap "Demo Quick Login" buttons on the login screen for testing both Admin and Student flows.*

---

## 6. Testing

### Run Backend Unit & Service Tests (19/19 Passing)
```bash
cd backend
mvn test
```

### Run Mobile Unit & Widget Tests (7/7 Passing)
```bash
cd mobile
flutter test
```

### Run Flutter Static Analysis
```bash
cd mobile
flutter analyze
```

---

## 7. Project Documentation Index

Comprehensive engineering documentation is available under `/docs`:
- [Project Assessment & Readiness](docs/assessment/project-assessment.md)
- [Product Overview & Requirements](docs/product/product-overview.md)
- [Business Rules & Billing Policies](docs/product/business-rules.md)
- [System Architecture & HLD](docs/architecture/hld.md)
- [Low-Level Design (LLD)](docs/architecture/lld.md)
- [Database ER Diagram & Schema](docs/database/er-diagram.md)
- [API Conventions & Endpoints](docs/api/api-overview.md)
- [Flutter Mobile Architecture](docs/mobile/flutter-architecture.md)
- [Spring Boot Architecture](docs/backend/spring-boot-architecture.md)
- [Architecture Decision Records (ADR-001 to ADR-006)](docs/decisions/)
- [Production Deployment Guide](docs/deployment/production-deployment.md)

---

## 8. License & Commercial Rights
Proprietary software developed for Digital Library Management System. All rights reserved.
