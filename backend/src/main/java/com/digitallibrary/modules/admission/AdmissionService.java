package com.digitallibrary.modules.admission;

import com.digitallibrary.core.errors.BadRequestException;
import com.digitallibrary.core.errors.ConflictException;
import com.digitallibrary.core.errors.ResourceNotFoundException;
import com.digitallibrary.modules.admission.dto.AdmissionRequestDto;
import com.digitallibrary.modules.admission.dto.AdmissionResponseDto;
import com.digitallibrary.modules.library.LibrarySettings;
import com.digitallibrary.modules.library.LibrarySettingsRepository;
import com.digitallibrary.modules.seat.Seat;
import com.digitallibrary.modules.seat.SeatRepository;
import com.digitallibrary.modules.seat.SeatStatus;
import com.digitallibrary.modules.subscription.Subscription;
import com.digitallibrary.modules.subscription.SubscriptionDto;
import com.digitallibrary.modules.subscription.SubscriptionService;
import com.digitallibrary.modules.user.User;
import com.digitallibrary.modules.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AdmissionService {

    private static final Logger log = LoggerFactory.getLogger(AdmissionService.class);

    private final AdmissionRepository admissionRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final LibrarySettingsRepository librarySettingsRepository;

    public AdmissionService(AdmissionRepository admissionRepository,
                            SeatRepository seatRepository,
                            UserRepository userRepository,
                            SubscriptionService subscriptionService,
                            LibrarySettingsRepository librarySettingsRepository) {
        this.admissionRepository = admissionRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.librarySettingsRepository = librarySettingsRepository;
    }

    @Transactional
    public AdmissionResponseDto processAdmission(AdmissionRequestDto request, Long studentId) {
        // 1. Verify student doesn't already have an active admission
        admissionRepository.findTopByStudentIdAndStatus(studentId, AdmissionStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new ConflictException("Student already has an active admission at Seat " + existing.getSeatId(), "ADMISSION_ALREADY_EXISTS");
                });

        // 2. Lock and verify seat
        Seat seat = seatRepository.findByIdWithLock(request.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", request.getSeatId()));

        if (seat.getStatus() == SeatStatus.OCCUPIED || seat.getStatus() == SeatStatus.MAINTENANCE) {
            throw new ConflictException("Seat " + seat.getSeatNumber() + " is not available", "SEAT_NOT_AVAILABLE");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));

        // 3. Mark seat OCCUPIED
        seat.setStatus(SeatStatus.OCCUPIED);
        seatRepository.save(seat);

        // 4. Save Admission record
        Admission admission = new Admission(
                request.getLibraryId(),
                studentId,
                seat.getId(),
                request.getJoiningDate(),
                request.getEmergencyContact()
        );
        admission = admissionRepository.save(admission);

        // 5. Initialize monthly subscription based on joining date
        BigDecimal feeAmount = librarySettingsRepository.findByLibraryId(request.getLibraryId())
                .map(LibrarySettings::getMonthlyFeeAmount)
                .orElse(new BigDecimal("700.00"));

        Subscription sub = subscriptionService.createInitialSubscription(
                request.getLibraryId(),
                studentId,
                request.getJoiningDate(),
                feeAmount
        );

        log.info("Admission confirmed for student {} at seat {}", student.getFullName(), seat.getSeatNumber());

        return AdmissionResponseDto.fromEntity(
                admission,
                student.getFullName(),
                student.getPhoneNumber(),
                seat.getSeatNumber(),
                SubscriptionDto.fromEntity(sub)
        );
    }

    @Transactional(readOnly = true)
    public AdmissionResponseDto getMyAdmission(Long studentId) {
        Admission admission = admissionRepository.findTopByStudentIdAndStatus(studentId, AdmissionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active admission found for current student"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));

        Seat seat = seatRepository.findById(admission.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", admission.getSeatId()));

        SubscriptionDto subscription = subscriptionService.getActiveSubscription(studentId);

        return AdmissionResponseDto.fromEntity(
                admission,
                student.getFullName(),
                student.getPhoneNumber(),
                seat.getSeatNumber(),
                subscription
        );
    }

    @Transactional
    public void cancelAdmission(Long admissionId, String reason) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission", "id", admissionId));

        admission.setStatus(AdmissionStatus.CANCELLED);
        admissionRepository.save(admission);

        // Release the seat
        seatRepository.findById(admission.getSeatId()).ifPresent(seat -> {
            seat.setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(seat);
            log.info("Seat {} released due to admission {} cancellation. Reason: {}", seat.getSeatNumber(), admissionId, reason);
        });
    }
}
