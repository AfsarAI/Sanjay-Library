# Digital Library Management Mobile Application - Project Assessment

**Document Version:** 1.0.0  
**Date:** 2026-09-24  
**Status:** Approved for Implementation  
**Target Deployment:** Production Digital Library (Self-Study Library, 50 Seats, 100+ Active Users, Village/City Context)

---

## 1. Current Repository Structure

At initialization, the repository `/home/afsarai/Library-Application` is clean and empty.
No legacy code, scaffolding, or configurations existed prior to this assessment.

```
/home/afsarai/Library-Application/
├── docs/                 # Documentation tree (HLD, LLD, Database, API, etc.)
```

---

## 2. Existing Code Assessment

- **Codebase status:** Clean slate (greenfield project).
- **Installed system capabilities:**
  - Java 25.0.1 LTS (JDK configured and active)
  - Maven 3.9.6 (Installed and verified)
  - Flutter & Dart SDK (Configured via Snap / Toolchain)
  - Docker 29.1.3 (Active and available for PostgreSQL, container testing, and deployment)
  - Git 2.43.0

---

## 3. Missing Components Required for Production

To realize the production-grade application for our client, the following components must be built end-to-end:

1. **Backend Service (`backend/`):**
   - Spring Boot 3.3.x / Java 21+ modular monolith.
   - Domain modules: Authentication, Users, Libraries, Seats, Admissions, Attendance, Subscriptions, Payments, Notifications, Reports, Audit Logs, Settings.
   - Database migrations (Flyway) for PostgreSQL.
   - Spring Security with Stateless JWT & Refresh Token rotation.
   - Payment integration (Razorpay Order creation, signature verification, webhook processing, idempotency keys).
   - Scheduled tasks for fee reminders, reservation timeouts, grace period transitions, seat release eligibility.
   - OpenAPI 3.0 / Swagger documentation.
   - Containerization via Docker & Compose.

2. **Mobile Client (`mobile/`):**
   - Flutter cross-platform application (targeting Android initially, iOS-ready).
   - State management: Riverpod with code-generation/state notifiers.
   - Navigation: GoRouter with deep-link and auth guard support.
   - Networking: Dio with token refresh interceptors, offline/retry policies, error serialization.
   - Secure storage: `flutter_secure_storage` for credentials and tokens.
   - Visual interactive seat map with real-time states (Available, Reserved, Occupied, Maintenance).
   - Dynamic QR scanner for attendance check-in/checkout.
   - Razorpay payment SDK integration with backend signature verification fallback.
   - Responsive layouts, dark/light themes, offline indicator banners.

3. **Database & Infrastructure (`docker/`, `scripts/`):**
   - PostgreSQL schema with strict foreign keys, check constraints, unique constraints, and audit fields.
   - Production Docker compose configuration with healthchecks, volume persistence, and connection pooling.
   - Automated database backup and restore scripts.
   - GitHub Actions CI/CD workflows for PR validation, linting, tests, and build artifacts.

4. **Documentation System (`docs/`):**
   - Full product specs, HLD, LLD, Database ERD & Data Dictionary, API documentation, ADRs, Admin Guide, and Deployment Runbooks.

---

## 4. Proposed Final Architecture

The system utilizes a **Modular Monolith** architecture. Microservices are deliberately avoided to maintain low operational overhead and transactional consistency for the 50-seat single-library operation while keeping the domain models cleanly decoupled and multi-library ready.

```
+-----------------------------------------------------------------------------------+
|                               FLUTTER MOBILE APP                                  |
|   (Android / iOS Cross-Platform | Riverpod State | GoRouter | Dio | SecureStorage)|
+-----------------------------------------------------------------------------------+
                                         │
                             HTTPS / JSON REST API (v1)
                                         │
+────────────────────────────────────────▼──────────────────────────────────────────+
|                        SPRING BOOT MODULAR MONOLITH                               |
|                                                                                   |
|  [Security & Auth Filter] ───> [Global Exception Handler] ───> [Auditing Aspect]  |
|                                                                                   |
|  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐ ┌──────────────┐  |
|  |   Auth Module    | |  Library Module  | |   Seat Module    | |Admission Mod |  |
|  └──────────────────┘ └──────────────────┘ └──────────────────┘ └──────────────┘  |
|  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐ ┌──────────────┐  |
|  | Attendance Module| | Subscription Mod | |  Payment Module  | | Notification |  |
|  └──────────────────┘ └──────────────────┘ └──────────────────┘ └──────────────┘  |
|  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐ ┌──────────────┐  |
|  |  Reports Module  | | Audit Log Module | |  Settings Module | | Scheduled Job|  |
|  └──────────────────┘ └──────────────────┘ └──────────────────┘ └──────────────┘  |
+────────────────────────────────────────┬──────────────────────────────────────────+
                                         │
                                  Spring Data JPA
                                         │
+────────────────────────────────────────▼──────────────────────────────────────────+
|                              POSTGRESQL DATABASE                                  |
| (Flyway Migrations | Row-Level Locking | Audit Triggers | Timezone UTC/Asia/Kolkata)|
+───────────────────────────────────────────────────────────────────────────────────+
```

---

## 5. Database & Domain Model

The schema enforces strict relational integrity with `library_id` multi-tenancy partitioning across all operational tables.

### Key Entities:
1. `libraries`: Library profile, operating hours, capacity, default policies.
2. `library_settings`: Configurable fee amounts, grace periods, seat release thresholds, reservation timeouts.
3. `users`: Credentials, phone numbers, email, encrypted passwords, roles (`ROLE_STUDENT`, `ROLE_ADMIN`, `ROLE_STAFF`), status.
4. `refresh_tokens`: Hashed refresh tokens with device IDs and expiry.
5. `students`: Student profile, emergency contacts, registration dates, active seat reference.
6. `seats`: Seat numbers (e.g., A01-A50), row/col coordinates, current status (`AVAILABLE`, `RESERVED`, `OCCUPIED`, `MAINTENANCE`), version for optimistic locking.
7. `seat_reservations`: Temporary bookings (10-minute timeout) with student/guest references and status.
8. `admissions`: Formal admission records linking student, seat, joining date, and subscription plan.
9. `subscriptions`: Joining-date-based billing cycles (`start_date`, `end_date`, `due_date`, `grace_until`, `status`: `ACTIVE`, `PAYMENT_DUE`, `OVERDUE`, `EXPIRED`, `EXTENDED`).
10. `payments`: Payment records (`gateway_order_id`, `gateway_payment_id`, `amount`, `method`: `ONLINE_RAZORPAY`, `CASH`, `status`, `idempotency_key`, `verified_at`).
11. `attendance_records`: Daily check-in/out records (`check_in_time`, `check_out_time`, `duration_minutes`, `date`, `verification_method`: `QR_ROTATING`, `MANUAL_ADMIN`, `STATUS`).
12. `attendance_qr_tokens`: Dynamic, rotating time-limited tokens generated by backend to prevent static QR replay.
13. `notifications`: In-app notification queue and logs (FCM push delivery status, email status).
14. `audit_logs`: Immutable trail of sensitive admin actions (`actor_id`, `action`, `entity_type`, `entity_id`, `old_value`, `new_value`, `ip_address`).

---

## 6. Flutter Architecture

A clean, feature-driven architecture ensuring strict separation between UI, state management, and network I/O:

```
lib/
├── app.dart                   # Root MaterialApp with theme & router
├── main.dart                  # App bootstrap, Riverpod ProviderScope, crash handling
├── core/
│   ├── config/                # Environment variables, API baseUrl, timeouts
│   ├── constants/             # Assets, colors, layout constants
│   ├── errors/                # Failure classes, exception mappers
│   ├── network/               # Dio client, Auth interceptor, Logging, RetryPolicy
│   ├── routing/               # GoRouter configuration, route guards (Auth, Role)
│   ├── security/              # SecureTokenStorage (flutter_secure_storage)
│   ├── theme/                 # AppTheme (Light & Dark mode, Material 3, typography)
│   └── utils/                 # Date formatters, currency formatters, validators
├── shared/
│   ├── widgets/               # PrimaryButton, CustomTextField, StatusBadge, EmptyStateCard, ErrorBanner
│   └── state/                 # ConnectivityProvider, CurrentUserProvider
└── features/
    ├── auth/                  # Login, OTP/Password, Splash, TokenRefresh
    ├── dashboard/             # Student home screen, Admin operational dashboard
    ├── seats/                 # Interactive visual seat layout grid, seat details modal
    ├── admission/             # Admission wizard (Personal info, Seat pick, Plan pick)
    ├── attendance/            # Scanner view, check-in card, monthly attendance calendar
    ├── subscriptions/         # Current cycle status, grace period warnings, renewal history
    ├── payments/              # Razorpay checkout bridge, Cash payment receipt viewer
    ├── notifications/         # In-app notification center, badge counters
    ├── profile/               # Student profile, emergency contact, library helpline
    └── admin/                 # Admin overrides (Seat swap, Cash record, Extension grant, Attendance fix)
```

---

## 7. Backend Architecture

Spring Boot 3.3.x application with layered clean architecture:

```
com.digitallibrary/
├── DigitalLibraryApplication.java
├── core/
│   ├── config/              # SecurityConfig, CorsConfig, OpenApiConfig, SchedulingConfig
│   ├── constants/           # Business defaults, role names, error codes
│   ├── errors/              # GlobalExceptionHandler, ApiException, ResourceNotFoundException
│   ├── security/            # JwtAuthenticationFilter, JwtTokenProvider, CustomUserDetailsService
│   └── utils/               # DateUtils, SecurityContextUtils, SignatureValidator
└── modules/
    ├── user/                # User entity, repo, UserDetails, UserDto
    ├── auth/                # AuthController, AuthService, LoginRequest, TokenResponse
    ├── library/             # Library, LibrarySettings entities, SettingsController, LibraryService
    ├── seat/                # Seat, SeatReservation entities, SeatController, SeatReservationService
    ├── admission/           # Admission entity, AdmissionController, AdmissionWorkflowService
    ├── attendance/          # AttendanceRecord, QrToken entities, AttendanceController, QrService
    ├── subscription/        # Subscription entity, SubscriptionController, BillingService
    ├── payment/             # Payment entity, PaymentController, RazorpayService, WebhookController
    ├── notification/        # Notification entity, NotificationService, PushProvider, EmailProvider
    ├── audit/               # AuditLog entity, AuditLogService, AuditableAspect
    ├── report/              # ReportController, ReportService (Occupancy, Revenue, Attendance)
    └── jobs/                # ScheduledJobService (Daily billing check, Expired reservation cleaner)
```

---

## 8. Development Phases

- **Phase 1: Project Foundation & Docs** (Workspace scaffold, Docker PostgreSQL, Maven backend, Flutter project, Core docs, Git config).
- **Phase 2: Authentication & Authorization** (Spring Security, JWT, Refresh Token, User entities, Flutter Auth flow & SecureStorage).
- **Phase 3: Library & Seat Management** (Interactive seat grid, seat entities, pessimistic/optimistic reservation locking, expiry cleaner).
- **Phase 4: Admission Flow** (Multi-step admission, seat assignment, admin alert).
- **Phase 5: Attendance System** (Check-in/out, rotating QR security, eligibility verification, admin overrides).
- **Phase 6: Subscriptions & Billing Engine** (Joining-date-based billing cycle calculation, grace period, overdue transitions).
- **Phase 7: Payments (Razorpay & Cash)** (Order creation, webhook signature validation, idempotency, cash receipt recording).
- **Phase 8: Notifications & Reminders** (Notification queue, push/email abstractions, scheduled fee reminders).
- **Phase 9: Admin Dashboard & Overrides** (Real-time operational dashboard, seat release, extension grant, audit logs).
- **Phase 10: Production Hardening, Testing & Verification** (Unit tests, integration tests, concurrency tests, rate limiting, error codes).
- **Phase 11: Deployment & CI/CD** (Dockerfiles, compose setup, GitHub Actions, backup/restore runbook).

---

## 9. Important Assumptions

1. **Physical Library Scale:** Single facility initially, 50 physical desks/seats, scalable up to 500+ seats without architectural modification.
2. **Joining-Date Billing:** Subscriptions are strictly individual cycles starting on the student's admission date (e.g., Aug 15 to Sept 14), rather than calendar-month billing.
3. **Currency & Localization:** INR (₹), Indian Standard Time (IST, UTC+05:30).
4. **Attendance Method:** Primary via mobile scanning of an in-library rotating QR code on a tablet/screen or dynamic authenticated check-in with geolocation verification; fallback manual admin entry.
5. **Payment Gateway:** Razorpay India (UPI, Netbanking, Cards, Wallets) with server-side HMAC-SHA256 signature verification.

---

## 10. Risks & Edge Cases

| Risk / Edge Case | Mitigation Strategy |
|---|---|
| Concurrent seat booking by two users | Database pessimistic locking (`SELECT ... FOR UPDATE`) or unique reservation constraint on `seat_id` with active status. |
| Duplicate Razorpay webhook callbacks | Strict payment idempotency check using `razorpay_payment_id` & database unique constraint on transaction ID. |
| Student check-in with overdue fee | Automatic check against `subscription.status` and `subscription.grace_until`; blocks check-in unless admin granted extension. |
| Static QR code photo sharing | Rotating QR token generated by server with 30-second TTL or geo-fencing validation. |
| Network drop during payment | Client sends order verification on app resume; backend reconciliation webhook guarantees subscription update regardless of client connectivity. |
| Expired reservation cleanup | Background scheduler runs every 60 seconds to release unconfirmed reservations older than 10 minutes. |

---

## 11. Documentation Plan

All documents are maintained under `/docs` as living architectural and operational artifacts:
- Product requirements, user roles, user journeys, business rules.
- High-Level Design (HLD) & Low-Level Design (LLD) with Mermaid diagrams.
- Database ERD, Data Dictionary, and Flyway migration guide.
- Complete OpenAPI/REST API specification with request/response samples.
- Mobile architecture, routing, and state management guide.
- Razorpay payment lifecycle and webhook security guide.
- Production deployment runbook, Docker configs, and backup/recovery strategies.
