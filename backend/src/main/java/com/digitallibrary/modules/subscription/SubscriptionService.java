package com.digitallibrary.modules.subscription;

import com.digitallibrary.core.errors.BadRequestException;
import com.digitallibrary.core.errors.ResourceNotFoundException;
import com.digitallibrary.modules.library.LibrarySettings;
import com.digitallibrary.modules.library.LibrarySettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final LibrarySettingsRepository librarySettingsRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               LibrarySettingsRepository librarySettingsRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.librarySettingsRepository = librarySettingsRepository;
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getActiveSubscription(Long studentId) {
        Subscription subscription = subscriptionRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("No subscription found for student id: " + studentId));
        return SubscriptionDto.fromEntity(subscription);
    }

    @Transactional
    public Subscription createInitialSubscription(Long libraryId, Long studentId, LocalDate joiningDate, BigDecimal amount) {
        int graceDays = librarySettingsRepository.findByLibraryId(libraryId)
                .map(LibrarySettings::getGracePeriodDays)
                .orElse(7);

        LocalDate startDate = joiningDate;
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        LocalDate dueDate = startDate.plusMonths(1);
        LocalDate graceUntil = dueDate.plusDays(graceDays);

        Subscription subscription = new Subscription(
                libraryId,
                studentId,
                startDate,
                endDate,
                dueDate,
                amount,
                SubscriptionStatus.ACTIVE
        );
        subscription.setGraceUntil(graceUntil);

        log.info("Initialized subscription for student {} from {} to {}, due on {}", studentId, startDate, endDate, dueDate);
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription advanceSubscriptionCycle(Long subscriptionId) {
        Subscription current = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));

        int graceDays = librarySettingsRepository.findByLibraryId(current.getLibraryId())
                .map(LibrarySettings::getGracePeriodDays)
                .orElse(7);

        // Advance anchor cycle by 1 month
        LocalDate newStartDate = current.getDueDate();
        LocalDate newEndDate = newStartDate.plusMonths(1).minusDays(1);
        LocalDate newDueDate = newStartDate.plusMonths(1);
        LocalDate newGraceUntil = newDueDate.plusDays(graceDays);

        current.setStartDate(newStartDate);
        current.setEndDate(newEndDate);
        current.setDueDate(newDueDate);
        current.setGraceUntil(newGraceUntil);
        current.setStatus(SubscriptionStatus.ACTIVE);

        log.info("Advanced subscription {} to cycle {} -> {}, next due {}", subscriptionId, newStartDate, newEndDate, newDueDate);
        return subscriptionRepository.save(current);
    }

    @Transactional
    public SubscriptionDto grantAdminExtension(Long subscriptionId, LocalDate newGraceUntil, String reason) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));

        if (newGraceUntil.isBefore(LocalDate.now())) {
            throw new BadRequestException("Extension date must be in the future", "INVALID_EXTENSION_DATE");
        }

        subscription.setGraceUntil(newGraceUntil);
        subscription.setStatus(SubscriptionStatus.EXTENDED);
        subscription = subscriptionRepository.save(subscription);

        log.info("Admin granted extension for subscription {} until {}. Reason: {}", subscriptionId, newGraceUntil, reason);
        return SubscriptionDto.fromEntity(subscription);
    }

    @Transactional
    public void processDailyBillingTransitions() {
        LocalDate today = LocalDate.now();

        // 1. Move ACTIVE to PAYMENT_DUE if dueDate is today or past
        List<Subscription> dueSubscriptions = subscriptionRepository.findActiveSubscriptionsDue(today);
        for (Subscription sub : dueSubscriptions) {
            sub.setStatus(SubscriptionStatus.PAYMENT_DUE);
            subscriptionRepository.save(sub);
            log.info("Subscription {} transitioned to PAYMENT_DUE", sub.getId());
        }

        // 2. Move OVERDUE to ATTENDANCE_BLOCKED if grace period has expired
        List<Subscription> pastGrace = subscriptionRepository.findOverdueSubscriptionsPastGrace(today);
        for (Subscription sub : pastGrace) {
            sub.setStatus(SubscriptionStatus.ATTENDANCE_BLOCKED);
            subscriptionRepository.save(sub);
            log.info("Subscription {} attendance BLOCKED due to expired grace period", sub.getId());
        }
    }
}
