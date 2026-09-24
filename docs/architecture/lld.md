# Digital Library Management System - Low-Level Design (LLD)

## 1. Domain Entities & Class Architecture

```mermaid
classDiagram
    class Library {
        +Long id
        +String name
        +String address
        +String contactPhone
        +LocalTime openingTime
        +LocalTime closingTime
        +Integer totalSeats
        +LocalDateTime createdAt
    }

    class LibrarySettings {
        +Long id
        +Long libraryId
        +BigDecimal monthlyFeeAmount
        +Integer gracePeriodDays
        +Integer attendanceBlockAfterDays
        +Integer seatReleaseAfterDays
        +Integer reservationTimeoutMinutes
        +Boolean allowQrAttendance
    }

    class User {
        +Long id
        +String phoneNumber
        +String email
        +String passwordHash
        +String fullName
        +UserRole role
        +UserStatus status
        +LocalDateTime createdAt
    }

    class Seat {
        +Long id
        +Long libraryId
        +String seatNumber
        +Integer rowNumber
        +Integer colNumber
        +SeatStatus status
        +Long version
    }

    class Admission {
        +Long id
        +Long libraryId
        +Long studentId
        +Long seatId
        +LocalDate joiningDate
        +AdmissionStatus status
        +LocalDateTime createdAt
    }

    class Subscription {
        +Long id
        +Long libraryId
        +Long studentId
        +LocalDate startDate
        +LocalDate endDate
        +LocalDate dueDate
        +LocalDate graceUntil
        +BigDecimal amount
        +SubscriptionStatus status
    }

    class Payment {
        +Long id
        +Long libraryId
        +Long studentId
        +Long subscriptionId
        +BigDecimal amount
        +PaymentMethod method
        +PaymentStatus status
        +String gatewayOrderId
        +String gatewayPaymentId
        +String idempotencyKey
        +LocalDateTime paymentDate
    }

    class AttendanceRecord {
        +Long id
        +Long libraryId
        +Long studentId
        +Long seatId
        +LocalDate date
        +LocalDateTime checkInTime
        +LocalDateTime checkOutTime
        +Integer durationMinutes
        +AttendanceStatus status
    }

    class AuditLog {
        +Long id
        +Long actorId
        +String action
        +String entityType
        +Long entityId
        +String oldValue
        +String newValue
        +String ipAddress
        +LocalDateTime timestamp
    }

    Library "1" -- "1" LibrarySettings
    Library "1" -- "*" Seat
    Library "1" -- "*" Admission
    User "1" -- "1" Admission
    Seat "1" -- "0..1" Admission
    User "1" -- "*" Subscription
    Subscription "1" -- "*" Payment
    User "1" -- "*" AttendanceRecord
```

---

## 2. Service Interfaces & Key Methods

### 2.1 `SeatReservationService`
```java
public interface SeatReservationService {
    SeatLayoutDto getLibrarySeatLayout(Long libraryId);
    SeatReservationDto reserveSeat(Long libraryId, Long seatId, Long userId);
    void releaseExpiredReservations();
    void confirmSeatBooking(Long seatId, Long studentId);
    void adminChangeSeat(Long libraryId, Long studentId, Long newSeatId, Long adminId);
}
```

### 2.2 `AdmissionService`
```java
public interface AdmissionService {
    AdmissionResponseDto processAdmission(AdmissionRequestDto request);
    AdmissionDetailsDto getAdmissionByStudentId(Long studentId);
    void cancelAdmission(Long admissionId, Long adminId, String reason);
}
```

### 2.3 `AttendanceService`
```java
public interface AttendanceService {
    QrTokenResponseDto generateRotatingQrToken(Long libraryId);
    AttendanceResponseDto checkInWithQr(Long studentId, String qrToken);
    AttendanceResponseDto checkOut(Long studentId);
    Page<AttendanceRecordDto> getStudentAttendanceHistory(Long studentId, Pageable pageable);
    TodayAttendanceSummaryDto getTodaySummary(Long libraryId);
    void adminCorrectAttendance(AttendanceCorrectionDto dto, Long adminId);
}
```

### 2.4 `SubscriptionService`
```java
public interface SubscriptionService {
    SubscriptionDto getActiveSubscription(Long studentId);
    void createInitialSubscription(Long studentId, LocalDate joiningDate, BigDecimal amount);
    void extendSubscriptionForPayment(Long studentId, Long paymentId);
    void processDailyBillingTransitions();
    void grantAdminExtension(Long subscriptionId, LocalDate newGraceUntil, String reason, Long adminId);
}
```

### 2.5 `PaymentService`
```java
public interface PaymentService {
    RazorpayOrderResponseDto createRazorpayOrder(Long studentId, Long subscriptionId);
    PaymentVerificationResponseDto verifyRazorpayPayment(PaymentVerificationRequestDto dto);
    void processRazorpayWebhook(String webhookPayload, String signatureHeader);
    PaymentDto recordCashPayment(CashPaymentRequestDto request, Long adminId);
}
```

---

## 3. Transaction Boundaries & Locks

- **Seat Reservation:** `@Transactional(isolation = Isolation.READ_COMMITTED)` with `PessimisticLockException` retry or explicit `LockModeType.PESSIMISTIC_WRITE` on `SeatRepository.findByIdWithLock(seatId)`.
- **Payment Verification:** `@Transactional` wrapping payment state update, subscription extension, and audit log write.
- **Cash Payment Recording:** Single atomic `@Transactional` method ensuring payment cannot be saved without simultaneously advancing the subscription.
- **Attendance Check-In:** `@Transactional` with `UNIQUE(student_id, date)` checking to prevent concurrent double check-in.
