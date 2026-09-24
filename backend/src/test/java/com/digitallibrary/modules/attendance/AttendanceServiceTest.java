package com.digitallibrary.modules.attendance;

import com.digitallibrary.core.errors.BadRequestException;
import com.digitallibrary.core.errors.ConflictException;
import com.digitallibrary.core.errors.ForbiddenException;
import com.digitallibrary.modules.admission.Admission;
import com.digitallibrary.modules.admission.AdmissionRepository;
import com.digitallibrary.modules.admission.AdmissionStatus;
import com.digitallibrary.modules.attendance.dto.AttendanceRecordDto;
import com.digitallibrary.modules.seat.Seat;
import com.digitallibrary.modules.seat.SeatRepository;
import com.digitallibrary.modules.subscription.Subscription;
import com.digitallibrary.modules.subscription.SubscriptionRepository;
import com.digitallibrary.modules.subscription.SubscriptionStatus;
import com.digitallibrary.modules.user.User;
import com.digitallibrary.modules.user.UserRepository;
import com.digitallibrary.modules.user.UserRole;
import com.digitallibrary.modules.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceQrTokenRepository qrTokenRepository;

    @Mock
    private AdmissionRepository admissionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SeatRepository seatRepository;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(
                attendanceRepository,
                qrTokenRepository,
                admissionRepository,
                subscriptionRepository,
                userRepository,
                seatRepository
        );
    }

    @Test
    @DisplayName("Should successfully check in with valid rotating QR token")
    void checkInWithQr_Success() {
        String tokenStr = "QR_VALID_TOKEN_123";
        AttendanceQrToken qrToken = new AttendanceQrToken(1L, tokenStr, "nonce123", Instant.now().plus(20, ChronoUnit.SECONDS));

        when(qrTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(qrToken));

        User student = new User(1L, "9876543210", null, "hash", "Rahul Kumar", UserRole.ROLE_STUDENT);
        student.setId(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(student));

        Admission admission = new Admission(1L, 100L, 25L, LocalDate.now().minusDays(10), "9999999999");
        when(admissionRepository.findTopByStudentIdAndStatus(100L, AdmissionStatus.ACTIVE)).thenReturn(Optional.of(admission));

        Subscription sub = new Subscription(1L, 100L, LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), LocalDate.now().plusDays(20), new BigDecimal("700.00"), SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findTopByStudentIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(sub));

        when(attendanceRepository.findByStudentIdAndDate(100L, LocalDate.now())).thenReturn(Optional.empty());

        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(i -> {
            AttendanceRecord r = i.getArgument(0);
            r.setId(999L);
            return r;
        });

        Seat seat = new Seat(1L, "A25", 3, 5, com.digitallibrary.modules.seat.SeatStatus.OCCUPIED);
        when(seatRepository.findById(25L)).thenReturn(Optional.of(seat));

        AttendanceRecordDto dto = attendanceService.checkInWithQr(100L, tokenStr);

        assertNotNull(dto);
        assertEquals(999L, dto.getId());
        assertEquals("Rahul Kumar", dto.getStudentName());
        assertEquals("A25", dto.getSeatNumber());
        assertEquals(AttendanceStatus.PRESENT, dto.getStatus());
    }

    @Test
    @DisplayName("Should reject check in if QR token is expired")
    void checkInWithQr_ExpiredToken_ThrowsBadRequest() {
        String tokenStr = "QR_EXPIRED_TOKEN";
        AttendanceQrToken qrToken = new AttendanceQrToken(1L, tokenStr, "nonce123", Instant.now().minus(5, ChronoUnit.SECONDS));

        when(qrTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(qrToken));

        assertThrows(BadRequestException.class, () -> attendanceService.checkInWithQr(100L, tokenStr));
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should block check in if subscription is ATTENDANCE_BLOCKED")
    void checkInWithQr_AttendanceBlocked_ThrowsForbidden() {
        String tokenStr = "QR_VALID";
        AttendanceQrToken qrToken = new AttendanceQrToken(1L, tokenStr, "nonce123", Instant.now().plus(20, ChronoUnit.SECONDS));

        when(qrTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(qrToken));

        User student = new User(1L, "9876543210", null, "hash", "Rahul Kumar", UserRole.ROLE_STUDENT);
        student.setId(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(student));

        Admission admission = new Admission(1L, 100L, 25L, LocalDate.now().minusDays(35), "9999999999");
        when(admissionRepository.findTopByStudentIdAndStatus(100L, AdmissionStatus.ACTIVE)).thenReturn(Optional.of(admission));

        Subscription sub = new Subscription(1L, 100L, LocalDate.now().minusDays(35), LocalDate.now().minusDays(5), LocalDate.now().minusDays(5), new BigDecimal("700.00"), SubscriptionStatus.ATTENDANCE_BLOCKED);
        when(subscriptionRepository.findTopByStudentIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(sub));

        assertThrows(ForbiddenException.class, () -> attendanceService.checkInWithQr(100L, tokenStr));
    }

    @Test
    @DisplayName("Should reject duplicate check in on the same day")
    void checkInWithQr_DuplicateCheckIn_ThrowsConflict() {
        String tokenStr = "QR_VALID";
        AttendanceQrToken qrToken = new AttendanceQrToken(1L, tokenStr, "nonce123", Instant.now().plus(20, ChronoUnit.SECONDS));

        when(qrTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(qrToken));

        User student = new User(1L, "9876543210", null, "hash", "Rahul Kumar", UserRole.ROLE_STUDENT);
        student.setId(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(student));

        Admission admission = new Admission(1L, 100L, 25L, LocalDate.now().minusDays(10), "9999999999");
        when(admissionRepository.findTopByStudentIdAndStatus(100L, AdmissionStatus.ACTIVE)).thenReturn(Optional.of(admission));

        Subscription sub = new Subscription(1L, 100L, LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), LocalDate.now().plusDays(20), new BigDecimal("700.00"), SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findTopByStudentIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(sub));

        AttendanceRecord existing = new AttendanceRecord(1L, 100L, 25L, LocalDate.now(), Instant.now().minus(2, ChronoUnit.HOURS), AttendanceVerificationMethod.QR_ROTATING);
        when(attendanceRepository.findByStudentIdAndDate(100L, LocalDate.now())).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> attendanceService.checkInWithQr(100L, tokenStr));
    }

    @Test
    @DisplayName("Should successfully checkout and calculate duration")
    void checkOut_Success() {
        Instant inTime = Instant.now().minus(180, ChronoUnit.MINUTES); // 3 hours ago
        AttendanceRecord record = new AttendanceRecord(1L, 100L, 25L, LocalDate.now(), inTime, AttendanceVerificationMethod.QR_ROTATING);

        when(attendanceRepository.findByStudentIdAndDate(100L, LocalDate.now())).thenReturn(Optional.of(record));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(i -> i.getArgument(0));

        AttendanceRecordDto dto = attendanceService.checkOut(100L);

        assertNotNull(dto);
        assertEquals(AttendanceStatus.CHECKED_OUT, dto.getStatus());
        assertNotNull(dto.getCheckOutTime());
        assertTrue(dto.getDurationMinutes() >= 179);
    }
}
