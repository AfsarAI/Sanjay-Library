package com.digitallibrary.modules.subscription;

import com.digitallibrary.modules.library.LibrarySettings;
import com.digitallibrary.modules.library.LibrarySettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private LibrarySettingsRepository librarySettingsRepository;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(
                subscriptionRepository,
                librarySettingsRepository
        );
    }

    @Test
    @DisplayName("Should accurately calculate subscription dates from student joining date")
    void createInitialSubscription_CalculatesDatesCorrectly() {
        LocalDate joiningDate = LocalDate.of(2026, 8, 15);
        BigDecimal amount = new BigDecimal("700.00");

        LibrarySettings settings = new LibrarySettings(1L, amount, 7, 8, 15, 10, true);
        when(librarySettingsRepository.findByLibraryId(1L)).thenReturn(Optional.of(settings));

        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> {
            Subscription s = i.getArgument(0);
            s.setId(1L);
            return s;
        });

        Subscription sub = subscriptionService.createInitialSubscription(1L, 100L, joiningDate, amount);

        assertNotNull(sub);
        assertEquals(LocalDate.of(2026, 8, 15), sub.getStartDate());
        assertEquals(LocalDate.of(2026, 9, 14), sub.getEndDate());
        assertEquals(LocalDate.of(2026, 9, 15), sub.getDueDate());
        assertEquals(LocalDate.of(2026, 9, 22), sub.getGraceUntil()); // +7 days grace
        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
    }

    @Test
    @DisplayName("Should advance billing cycle by 1 month on renewal payment")
    void advanceSubscriptionCycle_AdvancesMonthProperly() {
        Subscription current = new Subscription(
                1L, 100L,
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 9, 15),
                new BigDecimal("700.00"),
                SubscriptionStatus.PAYMENT_DUE
        );
        current.setId(10L);

        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(current));
        LibrarySettings settings = new LibrarySettings(1L, new BigDecimal("700.00"), 7, 8, 15, 10, true);
        when(librarySettingsRepository.findByLibraryId(1L)).thenReturn(Optional.of(settings));

        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription updated = subscriptionService.advanceSubscriptionCycle(10L);

        assertEquals(LocalDate.of(2026, 9, 15), updated.getStartDate());
        assertEquals(LocalDate.of(2026, 10, 14), updated.getEndDate());
        assertEquals(LocalDate.of(2026, 10, 15), updated.getDueDate());
        assertEquals(LocalDate.of(2026, 10, 22), updated.getGraceUntil());
        assertEquals(SubscriptionStatus.ACTIVE, updated.getStatus());
    }

    @Test
    @DisplayName("Should grant admin extension and re-enable attendance")
    void grantAdminExtension_Success() {
        Subscription current = new Subscription(
                1L, 100L,
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 9, 15),
                new BigDecimal("700.00"),
                SubscriptionStatus.ATTENDANCE_BLOCKED
        );
        current.setId(10L);

        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(current));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        LocalDate extensionDate = LocalDate.now().plusDays(5);
        SubscriptionDto dto = subscriptionService.grantAdminExtension(10L, extensionDate, "Exam week");

        assertNotNull(dto);
        assertEquals(SubscriptionStatus.EXTENDED, dto.getStatus());
        assertEquals(extensionDate, dto.getGraceUntil());
        assertTrue(dto.isAttendanceEligible());
    }
}
