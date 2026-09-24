package com.digitallibrary.modules.seat;

import com.digitallibrary.core.errors.BadRequestException;
import com.digitallibrary.core.errors.ConflictException;
import com.digitallibrary.core.errors.ResourceNotFoundException;
import com.digitallibrary.modules.library.Library;
import com.digitallibrary.modules.library.LibraryRepository;
import com.digitallibrary.modules.library.LibrarySettings;
import com.digitallibrary.modules.library.LibrarySettingsRepository;
import com.digitallibrary.modules.seat.dto.SeatDto;
import com.digitallibrary.modules.seat.dto.SeatLayoutDto;
import com.digitallibrary.modules.seat.dto.SeatReservationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SeatService {

    private static final Logger log = LoggerFactory.getLogger(SeatService.class);

    private final SeatRepository seatRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final LibraryRepository libraryRepository;
    private final LibrarySettingsRepository librarySettingsRepository;

    public SeatService(SeatRepository seatRepository,
                       SeatReservationRepository seatReservationRepository,
                       LibraryRepository libraryRepository,
                       LibrarySettingsRepository librarySettingsRepository) {
        this.seatRepository = seatRepository;
        this.seatReservationRepository = seatReservationRepository;
        this.libraryRepository = libraryRepository;
        this.librarySettingsRepository = librarySettingsRepository;
    }

    @Transactional(readOnly = true)
    public SeatLayoutDto getSeatLayout(Long libraryId) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Library", "id", libraryId));

        List<Seat> seats = seatRepository.findByLibraryIdOrderByRowNumberAscColNumberAsc(libraryId);
        List<SeatDto> seatDtos = seats.stream().map(SeatDto::fromEntity).toList();

        long available = seatRepository.countByLibraryIdAndStatus(libraryId, SeatStatus.AVAILABLE);
        long occupied = seatRepository.countByLibraryIdAndStatus(libraryId, SeatStatus.OCCUPIED);
        long reserved = seatRepository.countByLibraryIdAndStatus(libraryId, SeatStatus.RESERVED);
        long maintenance = seatRepository.countByLibraryIdAndStatus(libraryId, SeatStatus.MAINTENANCE);

        return new SeatLayoutDto(libraryId, library.getTotalSeats(), available, occupied, reserved, maintenance, seatDtos);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SeatReservationDto reserveSeat(Long libraryId, Long seatId, Long userId) {
        // Acquire pessimistic write lock on the seat to avoid race condition
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", seatId));

        if (!seat.getLibraryId().equals(libraryId)) {
            throw new BadRequestException("Seat does not belong to the specified library", "INVALID_SEAT_LIBRARY");
        }

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new ConflictException(
                    "Seat " + seat.getSeatNumber() + " is currently " + seat.getStatus() + ". Please select another seat.",
                    "SEAT_NOT_AVAILABLE"
            );
        }

        // Cancel any pending active reservations by this user
        seatReservationRepository.findByUserIdAndStatus(userId, SeatReservationStatus.PENDING)
                .ifPresent(existingReservation -> {
                    existingReservation.setStatus(SeatReservationStatus.CANCELLED);
                    seatReservationRepository.save(existingReservation);
                    seatRepository.findById(existingReservation.getSeatId()).ifPresent(s -> {
                        s.setStatus(SeatStatus.AVAILABLE);
                        seatRepository.save(s);
                    });
                });

        int timeoutMinutes = librarySettingsRepository.findByLibraryId(libraryId)
                .map(LibrarySettings::getReservationTimeoutMinutes)
                .orElse(10);

        Instant expiresAt = Instant.now().plus(timeoutMinutes, ChronoUnit.MINUTES);

        seat.setStatus(SeatStatus.RESERVED);
        seatRepository.save(seat);

        SeatReservation reservation = new SeatReservation(libraryId, seatId, userId, expiresAt);
        reservation = seatReservationRepository.save(reservation);

        log.info("Seat {} reserved by user {} until {}", seat.getSeatNumber(), userId, expiresAt);
        return SeatReservationDto.fromEntity(reservation, seat.getSeatNumber());
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        SeatReservation reservation = seatReservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("SeatReservation", "id", reservationId));

        if (!reservation.getUserId().equals(userId)) {
            throw new BadRequestException("You are not authorized to cancel this reservation", "UNAUTHORIZED_RESERVATION_CANCEL");
        }

        if (reservation.getStatus() == SeatReservationStatus.PENDING) {
            reservation.setStatus(SeatReservationStatus.CANCELLED);
            seatReservationRepository.save(reservation);

            seatRepository.findById(reservation.getSeatId()).ifPresent(seat -> {
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            });
            log.info("Reservation {} cancelled by user {}", reservationId, userId);
        }
    }

    @Transactional
    public void releaseExpiredReservations() {
        List<SeatReservation> expired = seatReservationRepository.findExpiredPendingReservations(Instant.now());
        for (SeatReservation reservation : expired) {
            reservation.setStatus(SeatReservationStatus.EXPIRED);
            seatReservationRepository.save(reservation);

            seatRepository.findById(reservation.getSeatId()).ifPresent(seat -> {
                if (seat.getStatus() == SeatStatus.RESERVED) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seatRepository.save(seat);
                    log.info("Reservation {} expired. Seat {} reset to AVAILABLE", reservation.getId(), seat.getSeatNumber());
                }
            });
        }
    }

    @Transactional
    public SeatDto adminUpdateSeatStatus(Long seatId, SeatStatus newStatus) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", seatId));

        seat.setStatus(newStatus);
        seat = seatRepository.save(seat);
        log.info("Admin updated seat {} status to {}", seat.getSeatNumber(), newStatus);
        return SeatDto.fromEntity(seat);
    }
}
