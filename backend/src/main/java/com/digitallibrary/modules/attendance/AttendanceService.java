package com.digitallibrary.modules.attendance;

import com.digitallibrary.core.errors.BadRequestException;
import com.digitallibrary.core.errors.ConflictException;
import com.digitallibrary.core.errors.ForbiddenException;
import com.digitallibrary.core.errors.ResourceNotFoundException;
import com.digitallibrary.modules.admission.Admission;
import com.digitallibrary.modules.admission.AdmissionRepository;
import com.digitallibrary.modules.admission.AdmissionStatus;
import com.digitallibrary.modules.attendance.dto.*;
import com.digitallibrary.modules.seat.Seat;
import com.digitallibrary.modules.seat.SeatRepository;
import com.digitallibrary.modules.subscription.Subscription;
import com.digitallibrary.modules.subscription.SubscriptionRepository;
import com.digitallibrary.modules.subscription.SubscriptionStatus;
import com.digitallibrary.modules.user.User;
import com.digitallibrary.modules.user.UserRepository;
import com.digitallibrary.modules.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);

    private final AttendanceRepository attendanceRepository;
    private final AttendanceQrTokenRepository qrTokenRepository;
    private final AdmissionRepository admissionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             AttendanceQrTokenRepository qrTokenRepository,
                             AdmissionRepository admissionRepository,
                             SubscriptionRepository subscriptionRepository,
                             UserRepository userRepository,
                             SeatRepository seatRepository) {
        this.attendanceRepository = attendanceRepository;
        this.qrTokenRepository = qrTokenRepository;
        this.admissionRepository = admissionRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public AttendanceQrTokenResponseDto generateRotatingQrToken(Long libraryId) {
        String token = "QR_" + UUID.randomUUID().toString().replace("-", "");
        String nonce = UUID.randomUUID().toString().substring(0, 16);
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.SECONDS);

        AttendanceQrToken qrToken = new AttendanceQrToken(libraryId, token, nonce, expiresAt);
        qrToken = qrTokenRepository.save(qrToken);

        return new AttendanceQrTokenResponseDto(qrToken.getToken(), qrToken.getExpiresAt(), 30);
    }

    @Transactional
    public AttendanceRecordDto checkInWithQr(Long studentId, String qrToken) {
        // 1. Verify dynamic QR token
        AttendanceQrToken tokenEntity = qrTokenRepository.findByToken(qrToken)
                .orElseThrow(() -> new BadRequestException("Invalid or unrecognized QR token", "INVALID_QR_TOKEN"));

        if (tokenEntity.isExpired()) {
            throw new BadRequestException("QR Code expired. Please scan the current code displayed on the screen.", "QR_CODE_EXPIRED");
        }

        return executeCheckIn(studentId, tokenEntity.getLibraryId(), AttendanceVerificationMethod.QR_ROTATING);
    }

    @Transactional
    public AttendanceRecordDto executeCheckIn(Long studentId, Long libraryId, AttendanceVerificationMethod method) {
        LocalDate today = LocalDate.now();

        // 2. Validate Student status
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        if (student.getStatus() == UserStatus.SUSPENDED) {
            throw new ForbiddenException("Account is suspended. Contact administration.", "ACCOUNT_SUSPENDED");
        }

        // 3. Validate active admission
        Admission admission = admissionRepository.findTopByStudentIdAndStatus(studentId, AdmissionStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("No active seat admission found. Please complete admission first.", "NO_ACTIVE_ADMISSION"));

        // 4. Validate subscription eligibility
        Subscription subscription = subscriptionRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId)
                .orElseThrow(() -> new BadRequestException("No subscription found for student", "NO_SUBSCRIPTION"));

        if (subscription.getStatus() == SubscriptionStatus.ATTENDANCE_BLOCKED) {
            throw new ForbiddenException(
                    "Attendance is BLOCKED because your fee is overdue. Please pay your fee or request an extension from the library owner.",
                    "OVERDUE_ATTENDANCE_BLOCKED"
            );
        }

        if (subscription.getStatus() == SubscriptionStatus.RELEASED) {
            throw new ForbiddenException("Seat has been released due to prolonged non-payment.", "SEAT_RELEASED");
        }

        // Check grace cutoff if in OVERDUE state
        if (subscription.getStatus() == SubscriptionStatus.OVERDUE &&
                subscription.getGraceUntil() != null && today.isAfter(subscription.getGraceUntil())) {
            throw new ForbiddenException("Attendance grace period expired. Fee payment required.", "OVERDUE_ATTENDANCE_BLOCKED");
        }

        // 5. Prevent duplicate check-in today
        Optional<AttendanceRecord> existingOpt = attendanceRepository.findByStudentIdAndDate(studentId, today);
        if (existingOpt.isPresent()) {
            AttendanceRecord existing = existingOpt.get();
            if (existing.getCheckOutTime() == null) {
                throw new ConflictException("You are already checked in for today at " + existing.getCheckInTime(), "ALREADY_CHECKED_IN");
            } else {
                throw new ConflictException("You have already checked out and completed your attendance session for today.", "DAILY_SESSION_COMPLETED");
            }
        }

        // 6. Record Check-In
        AttendanceRecord record = new AttendanceRecord(
                libraryId,
                studentId,
                admission.getSeatId(),
                today,
                Instant.now(),
                method
        );
        record = attendanceRepository.save(record);

        Seat seat = seatRepository.findById(admission.getSeatId()).orElse(null);
        String seatNumber = seat != null ? seat.getSeatNumber() : "Unknown";

        log.info("Student {} checked in at {} for seat {}", student.getFullName(), record.getCheckInTime(), seatNumber);
        return AttendanceRecordDto.fromEntity(record, student.getFullName(), seatNumber);
    }

    @Transactional
    public AttendanceRecordDto checkOut(Long studentId) {
        LocalDate today = LocalDate.now();

        AttendanceRecord record = attendanceRepository.findByStudentIdAndDate(studentId, today)
                .orElseThrow(() -> new BadRequestException("No check-in record found for today", "NO_ACTIVE_CHECKIN"));

        if (record.getCheckOutTime() != null) {
            throw new BadRequestException("You have already checked out today at " + record.getCheckOutTime(), "ALREADY_CHECKED_OUT");
        }

        Instant checkOutTime = Instant.now();
        record.setCheckOutTime(checkOutTime);
        long duration = Duration.between(record.getCheckInTime(), checkOutTime).toMinutes();
        record.setDurationMinutes((int) duration);
        record.setStatus(AttendanceStatus.CHECKED_OUT);
        record = attendanceRepository.save(record);

        User student = userRepository.findById(studentId).orElse(null);
        Seat seat = seatRepository.findById(record.getSeatId()).orElse(null);

        log.info("Student {} checked out at {}. Duration: {} minutes",
                student != null ? student.getFullName() : studentId, checkOutTime, duration);

        return AttendanceRecordDto.fromEntity(
                record,
                student != null ? student.getFullName() : "",
                seat != null ? seat.getSeatNumber() : ""
        );
    }

    @Transactional(readOnly = true)
    public Page<AttendanceRecordDto> getStudentAttendanceHistory(Long studentId, Pageable pageable) {
        User student = userRepository.findById(studentId).orElse(null);
        String studentName = student != null ? student.getFullName() : "";

        return attendanceRepository.findByStudentIdOrderByDateDesc(studentId, pageable)
                .map(r -> {
                    Seat seat = seatRepository.findById(r.getSeatId()).orElse(null);
                    return AttendanceRecordDto.fromEntity(r, studentName, seat != null ? seat.getSeatNumber() : "");
                });
    }

    @Transactional(readOnly = true)
    public TodayAttendanceSummaryDto getTodaySummary(Long libraryId) {
        LocalDate today = LocalDate.now();

        long totalAdmitted = admissionRepository.countByLibraryIdAndStatus(libraryId, AdmissionStatus.ACTIVE);
        long checkedInToday = attendanceRepository.countByLibraryIdAndDate(libraryId, today);
        long currentlyInside = attendanceRepository.countCurrentlyInside(libraryId, today);
        long checkedOut = checkedInToday - currentlyInside;
        long absent = Math.max(0, totalAdmitted - checkedInToday);

        List<AttendanceRecord> records = attendanceRepository.findByLibraryIdAndDate(libraryId, today);
        List<AttendanceRecordDto> recordDtos = records.stream().map(r -> {
            User u = userRepository.findById(r.getStudentId()).orElse(null);
            Seat s = seatRepository.findById(r.getSeatId()).orElse(null);
            return AttendanceRecordDto.fromEntity(
                    r,
                    u != null ? u.getFullName() : "",
                    s != null ? s.getSeatNumber() : ""
            );
        }).toList();

        return new TodayAttendanceSummaryDto(
                libraryId, today, totalAdmitted, checkedInToday, currentlyInside, checkedOut, absent, recordDtos
        );
    }
}
