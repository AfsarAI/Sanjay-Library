package com.digitallibrary.modules.seat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {

    Optional<SeatReservation> findBySeatIdAndStatus(Long seatId, SeatReservationStatus status);

    Optional<SeatReservation> findByUserIdAndStatus(Long userId, SeatReservationStatus status);

    @Query("SELECT r FROM SeatReservation r WHERE r.status = 'PENDING' AND r.expiresAt < :now")
    List<SeatReservation> findExpiredPendingReservations(@Param("now") Instant now);
}
