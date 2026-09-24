package com.digitallibrary.modules.seat;

import com.digitallibrary.core.errors.ConflictException;
import com.digitallibrary.modules.library.Library;
import com.digitallibrary.modules.library.LibraryRepository;
import com.digitallibrary.modules.library.LibrarySettings;
import com.digitallibrary.modules.library.LibrarySettingsRepository;
import com.digitallibrary.modules.seat.dto.SeatLayoutDto;
import com.digitallibrary.modules.seat.dto.SeatReservationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatReservationRepository seatReservationRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private LibrarySettingsRepository librarySettingsRepository;

    private SeatService seatService;

    @BeforeEach
    void setUp() {
        seatService = new SeatService(
                seatRepository,
                seatReservationRepository,
                libraryRepository,
                librarySettingsRepository
        );
    }

    @Test
    @DisplayName("Should successfully reserve available seat")
    void reserveSeat_Success() {
        Seat seat = new Seat(1L, "A23", 3, 3, SeatStatus.AVAILABLE);
        seat.setId(23L);

        when(seatRepository.findByIdWithLock(23L)).thenReturn(Optional.of(seat));
        when(seatReservationRepository.findByUserIdAndStatus(100L, SeatReservationStatus.PENDING))
                .thenReturn(Optional.empty());

        LibrarySettings settings = new LibrarySettings(1L, new BigDecimal("700.00"), 7, 8, 15, 10, true);
        when(librarySettingsRepository.findByLibraryId(1L)).thenReturn(Optional.of(settings));

        when(seatReservationRepository.save(any(SeatReservation.class))).thenAnswer(i -> {
            SeatReservation r = i.getArgument(0);
            r.setId(500L);
            return r;
        });

        SeatReservationDto dto = seatService.reserveSeat(1L, 23L, 100L);

        assertNotNull(dto);
        assertEquals("A23", dto.getSeatNumber());
        assertEquals(SeatStatus.RESERVED, seat.getStatus());
        verify(seatRepository).save(seat);
    }

    @Test
    @DisplayName("Should reject reservation if seat is already occupied")
    void reserveSeat_AlreadyOccupied_ThrowsConflict() {
        Seat seat = new Seat(1L, "A23", 3, 3, SeatStatus.OCCUPIED);
        seat.setId(23L);

        when(seatRepository.findByIdWithLock(23L)).thenReturn(Optional.of(seat));

        assertThrows(ConflictException.class, () -> seatService.reserveSeat(1L, 23L, 100L));
        verify(seatReservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return full seat layout map with counts")
    void getSeatLayout_ReturnsLayout() {
        Library library = new Library();
        library.setId(1L);
        library.setTotalSeats(50);

        Seat seat = new Seat(1L, "A01", 1, 1, SeatStatus.AVAILABLE);
        seat.setId(1L);

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(library));
        when(seatRepository.findByLibraryIdOrderByRowNumberAscColNumberAsc(1L)).thenReturn(List.of(seat));
        when(seatRepository.countByLibraryIdAndStatus(1L, SeatStatus.AVAILABLE)).thenReturn(40L);
        when(seatRepository.countByLibraryIdAndStatus(1L, SeatStatus.OCCUPIED)).thenReturn(10L);
        when(seatRepository.countByLibraryIdAndStatus(1L, SeatStatus.RESERVED)).thenReturn(0L);
        when(seatRepository.countByLibraryIdAndStatus(1L, SeatStatus.MAINTENANCE)).thenReturn(0L);

        SeatLayoutDto layout = seatService.getSeatLayout(1L);

        assertNotNull(layout);
        assertEquals(50, layout.getTotalSeats());
        assertEquals(40L, layout.getAvailableSeats());
        assertEquals(10L, layout.getOccupiedSeats());
        assertEquals(1, layout.getSeats().size());
    }
}
