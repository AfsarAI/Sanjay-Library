# Spring Boot Modular Monolith Architecture

## 1. Architecture Overview
The backend is structured as a **Modular Monolith** built on **Java 21+** and **Spring Boot 3.3.x**, following Domain-Driven Design (DDD) principles. Modules are loosely coupled and organized by functional domain.

```
backend/src/main/java/com/digitallibrary/
  ├── core/
  │   ├── config/          # SecurityConfig, CorsConfig, OpenApiConfig, DatabaseSeeder
  │   ├── dto/             # Universal ApiResponse & ErrorResponse wrappers
  │   ├── errors/          # GlobalExceptionHandler, ApiException, ResourceNotFoundException
  │   └── security/        # JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal
  └── modules/
      ├── admin/           # AdminService, AdminController, metrics & facility operations
      ├── admission/       # AdmissionService, student enrollment & seat assignment
      ├── attendance/      # AttendanceService, rotating QR cryptographic validation
      ├── audit/           # AuditLogService, persistent immutable audit trail
      ├── auth/            # AuthService, AuthController, login, registration, refresh
      ├── jobs/            # ScheduledBillingJob, ScheduledReservationCleanupJob
      ├── notification/    # NotificationService with extensible notification adapters
      ├── payment/         # PaymentService, Razorpay order/verification, cash ledger
      ├── seat/            # SeatService, pessimistic locking reservation repository
      ├── subscription/    # SubscriptionService, joining-date anchor lifecycle
      └── user/            # User, RefreshToken, UserRepository
```

---

## 2. Concurrency & Seat Locking
To prevent race conditions where two students simultaneously select the same physical desk (e.g. Desk A23):
- The `SeatRepository` implements a **Pessimistic Write Lock**:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Seat s WHERE s.id = :id AND s.libraryId = :libraryId")
Optional<Seat> findByIdWithLock(@Param("id") Long id, @Param("libraryId") Long libraryId);
```
- The transaction runs with isolation level `READ_COMMITTED` inside `SeatService.reserveSeat()`.
- If another transaction is in progress, the second request safely waits or immediately fails with `SEAT_ALREADY_RESERVED`.

---

## 3. Scheduled Background Jobs
Spring Scheduler runs recurring background tasks:
1. **ScheduledReservationCleanupJob** (Runs every minute):
   - Queries `seat_reservations` where `status = 'ACTIVE'` and `expires_at < NOW()`.
   - Expires the hold and resets the seat status back to `AVAILABLE`.
2. **ScheduledBillingJob** (Runs daily at midnight):
   - Evaluates active subscriptions against current library policy (`grace_period_days = 7`, `attendance_block_after_days = 8`, `seat_release_after_days = 15`).
   - Automatically transitions overdue students to `ATTENDANCE_BLOCKED` and flags prolonged overdue seats for release.

---

## 4. Multi-Tenant Preparedness
All primary entities (`seats`, `students`, `admissions`, `subscriptions`, `attendance_records`, `payments`) maintain a `library_id` foreign key. This ensures the monolith can be scaled or partitioned across multiple physical library branches seamlessly.
