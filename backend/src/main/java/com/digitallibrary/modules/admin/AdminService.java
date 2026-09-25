package com.digitallibrary.modules.admin;

import com.digitallibrary.core.errors.BadRequestException;
import com.digitallibrary.core.errors.ConflictException;
import com.digitallibrary.core.errors.ResourceNotFoundException;
import com.digitallibrary.modules.admin.dto.ActionItemDto;
import com.digitallibrary.modules.admin.dto.AdminDashboardDto;
import com.digitallibrary.modules.admin.dto.AdminStudentDirectoryDto;
import com.digitallibrary.modules.admission.Admission;
import com.digitallibrary.modules.admission.AdmissionRepository;
import com.digitallibrary.modules.admission.AdmissionStatus;
import com.digitallibrary.modules.attendance.AttendanceRepository;
import com.digitallibrary.modules.audit.AuditLogService;
import com.digitallibrary.modules.library.Library;
import com.digitallibrary.modules.library.LibraryRepository;
import com.digitallibrary.modules.payment.PaymentRepository;
import com.digitallibrary.modules.seat.Seat;
import com.digitallibrary.modules.seat.SeatRepository;
import com.digitallibrary.modules.seat.SeatStatus;
import com.digitallibrary.modules.subscription.Subscription;
import com.digitallibrary.modules.subscription.SubscriptionRepository;
import com.digitallibrary.modules.subscription.SubscriptionStatus;
import com.digitallibrary.modules.user.User;
import com.digitallibrary.modules.user.UserRepository;
import com.digitallibrary.modules.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final LibraryRepository libraryRepository;
    private final SeatRepository seatRepository;
    private final AttendanceRepository attendanceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AdmissionRepository admissionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminService(LibraryRepository libraryRepository,
                        SeatRepository seatRepository,
                        AttendanceRepository attendanceRepository,
                        SubscriptionRepository subscriptionRepository,
                        AdmissionRepository admissionRepository,
                        PaymentRepository paymentRepository,
                        UserRepository userRepository,
                        AuditLogService auditLogService) {
        this.libraryRepository = libraryRepository;
        this.seatRepository = seatRepository;
        this.attendanceRepository = attendanceRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.admissionRepository = admissionRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboard(Long libraryId) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Library", "id", libraryId));

        LocalDate today = LocalDate.now();

        // 1. Seat breakdown
        long occupied = seatRepository.countByLibraryIdAndStatus(libraryId, SeatStatus.OCCUPIED);
        long available = seatRepository.countByLibraryIdAndStatus(libraryId, SeatStatus.AVAILABLE);
        long reserved = seatRepository.countByLibraryIdAndStatus(libraryId, SeatStatus.RESERVED);
        long maintenance = seatRepository.countByLibraryIdAndStatus(libraryId, SeatStatus.MAINTENANCE);

        // 2. Attendance
        long todayAttendance = attendanceRepository.countByLibraryIdAndDate(libraryId, today);
        long currentlyInside = attendanceRepository.countCurrentlyInside(libraryId, today);

        // 3. Billing metrics
        long feesDue = subscriptionRepository.countByLibraryIdAndStatus(libraryId, SubscriptionStatus.PAYMENT_DUE);
        long overdue = subscriptionRepository.countByLibraryIdAndStatus(libraryId, SubscriptionStatus.OVERDUE) +
                subscriptionRepository.countByLibraryIdAndStatus(libraryId, SubscriptionStatus.ATTENDANCE_BLOCKED);
        long newAdmissions = admissionRepository.countByLibraryIdAndStatus(libraryId, AdmissionStatus.ACTIVE);

        // 4. Revenue (30 days)
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        BigDecimal revenue = paymentRepository.sumSuccessfulRevenueSince(libraryId, thirtyDaysAgo);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        BigDecimal pendingRevenue = BigDecimal.valueOf(feesDue + overdue).multiply(new BigDecimal("700.00"));

        // 5. Action Required Items
        List<ActionItemDto> actionItems = new ArrayList<>();

        List<Subscription> overdueSubs = subscriptionRepository.findByLibraryIdAndStatus(libraryId, SubscriptionStatus.OVERDUE);
        for (Subscription sub : overdueSubs) {
            User student = userRepository.findById(sub.getStudentId()).orElse(null);
            Admission adm = admissionRepository.findTopByStudentIdAndStatus(sub.getStudentId(), AdmissionStatus.ACTIVE).orElse(null);
            String seatNum = (adm != null) ? getSeatNumber(adm.getSeatId()) : "N/A";
            actionItems.add(new ActionItemDto(
                    "OVERDUE",
                    "Fee Overdue: " + (student != null ? student.getFullName() : "Student"),
                    "Payment past due date (" + sub.getDueDate() + "). Grace active until " + sub.getGraceUntil(),
                    sub.getId(),
                    student != null ? student.getFullName() : "",
                    student != null ? student.getPhoneNumber() : "",
                    seatNum
            ));
        }

        List<Subscription> blockedSubs = subscriptionRepository.findByLibraryIdAndStatus(libraryId, SubscriptionStatus.ATTENDANCE_BLOCKED);
        for (Subscription sub : blockedSubs) {
            User student = userRepository.findById(sub.getStudentId()).orElse(null);
            Admission adm = admissionRepository.findTopByStudentIdAndStatus(sub.getStudentId(), AdmissionStatus.ACTIVE).orElse(null);
            String seatNum = (adm != null) ? getSeatNumber(adm.getSeatId()) : "N/A";
            actionItems.add(new ActionItemDto(
                    "ATTENDANCE_BLOCKED",
                    "Attendance Blocked: " + (student != null ? student.getFullName() : "Student"),
                    "Grace period expired. Student cannot check in unless extension granted.",
                    sub.getId(),
                    student != null ? student.getFullName() : "",
                    student != null ? student.getPhoneNumber() : "",
                    seatNum
            ));
        }

        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setLibraryId(libraryId);
        dto.setTotalSeats(library.getTotalSeats());
        dto.setOccupiedSeats(occupied);
        dto.setAvailableSeats(available);
        dto.setReservedSeats(reserved);
        dto.setMaintenanceSeats(maintenance);
        dto.setTodayAttendance(todayAttendance);
        dto.setCurrentlyInside(currentlyInside);
        dto.setFeesDueCount(feesDue);
        dto.setOverdueCount(overdue);
        dto.setNewAdmissionsCount(newAdmissions);
        dto.setCurrentMonthRevenue(revenue);
        dto.setPendingRevenue(pendingRevenue);
        dto.setActionRequired(actionItems);

        return dto;
    }

    @Transactional
    public void changeStudentSeat(Long libraryId, Long studentId, Long newSeatId, Long adminId) {
        Admission admission = admissionRepository.findTopByStudentIdAndStatus(studentId, AdmissionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active admission not found for student " + studentId));

        Seat newSeat = seatRepository.findByIdWithLock(newSeatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", newSeatId));

        if (newSeat.getStatus() != SeatStatus.AVAILABLE) {
            throw new ConflictException("New seat " + newSeat.getSeatNumber() + " is not available", "SEAT_NOT_AVAILABLE");
        }

        Seat oldSeat = seatRepository.findById(admission.getSeatId()).orElse(null);
        String oldSeatNumber = oldSeat != null ? oldSeat.getSeatNumber() : "Unknown";

        // Free old seat
        if (oldSeat != null) {
            oldSeat.setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(oldSeat);
        }

        // Occupy new seat
        newSeat.setStatus(SeatStatus.OCCUPIED);
        seatRepository.save(newSeat);

        // Update admission
        admission.setSeatId(newSeatId);
        admissionRepository.save(admission);

        auditLogService.logAction(
                libraryId,
                adminId,
                "SEAT_SWAP",
                "ADMISSION",
                admission.getId(),
                "Old Seat: " + oldSeatNumber,
                "New Seat: " + newSeat.getSeatNumber(),
                "ADMIN"
        );

        log.info("Admin {} changed student {} seat from {} to {}", adminId, studentId, oldSeatNumber, newSeat.getSeatNumber());
    }

    @Transactional
    public void releaseSeat(Long libraryId, Long seatId, Long adminId, String reason) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", seatId));

        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        admissionRepository.findTopBySeatIdAndStatus(seatId, AdmissionStatus.ACTIVE).ifPresent(adm -> {
            adm.setStatus(AdmissionStatus.COMPLETED);
            admissionRepository.save(adm);

            subscriptionRepository.findTopByStudentIdOrderByCreatedAtDesc(adm.getStudentId()).ifPresent(sub -> {
                sub.setStatus(SubscriptionStatus.RELEASED);
                subscriptionRepository.save(sub);
            });
        });

        auditLogService.logAction(
                libraryId,
                adminId,
                "SEAT_RELEASE",
                "SEAT",
                seatId,
                "OCCUPIED",
                "AVAILABLE (Reason: " + reason + ")",
                "ADMIN"
        );

        log.info("Admin {} released seat {}. Reason: {}", adminId, seat.getSeatNumber(), reason);
    }

    @Transactional
    public void toggleStudentStatus(Long studentId, UserStatus newStatus, Long adminId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));

        UserStatus oldStatus = user.getStatus();
        user.setStatus(newStatus);
        userRepository.save(user);

        auditLogService.logAction(
                user.getLibraryId(),
                adminId,
                "UPDATE_STUDENT_STATUS",
                "USER",
                studentId,
                oldStatus.name(),
                newStatus.name(),
                "ADMIN"
        );

        log.info("Admin {} updated student {} status from {} to {}", adminId, studentId, oldStatus, newStatus);
    }

    private String getSeatNumber(Long seatId) {
        return seatRepository.findById(seatId).map(Seat::getSeatNumber).orElse("Unknown");
    }

    @Transactional(readOnly = true)
    public List<AdminStudentDirectoryDto> getStudentDirectory(Long libraryId, String query, String filterStatus) {
        List<User> students = userRepository.findAll().stream()
                .filter(u -> libraryId.equals(u.getLibraryId()) && u.getRole() == com.digitallibrary.modules.user.UserRole.ROLE_STUDENT)
                .toList();

        List<AdminStudentDirectoryDto> list = new ArrayList<>();
        for (User u : students) {
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase().trim();
                boolean matchesName = u.getFullName() != null && u.getFullName().toLowerCase().contains(q);
                boolean matchesPhone = u.getPhoneNumber() != null && u.getPhoneNumber().contains(q);
                if (!matchesName && !matchesPhone) {
                    continue;
                }
            }

            Admission admission = admissionRepository.findTopByStudentIdAndStatus(u.getId(), AdmissionStatus.ACTIVE).orElse(null);
            String seatNumber = "None";
            Long seatId = null;
            if (admission != null) {
                seatId = admission.getSeatId();
                seatNumber = getSeatNumber(admission.getSeatId());
            }

            Subscription subscription = subscriptionRepository.findTopByStudentIdOrderByCreatedAtDesc(u.getId()).orElse(null);
            SubscriptionStatus subStatus = subscription != null ? subscription.getStatus() : null;
            LocalDate dueDate = subscription != null ? subscription.getDueDate() : null;
            LocalDate graceUntil = subscription != null ? subscription.getGraceUntil() : null;

            if (filterStatus != null && !filterStatus.isBlank() && !"ALL".equalsIgnoreCase(filterStatus)) {
                if ("SUSPENDED".equalsIgnoreCase(filterStatus)) {
                    if (u.getStatus() != UserStatus.SUSPENDED) continue;
                } else if (subStatus == null || !subStatus.name().equalsIgnoreCase(filterStatus)) {
                    continue;
                }
            }

            list.add(new AdminStudentDirectoryDto(
                    u.getId(),
                    u.getFullName(),
                    u.getPhoneNumber(),
                    u.getEmail(),
                    seatNumber,
                    seatId,
                    u.getStatus(),
                    subStatus,
                    dueDate,
                    graceUntil,
                    u.getCreatedAt()
            ));
        }

        return list;
    }
}
