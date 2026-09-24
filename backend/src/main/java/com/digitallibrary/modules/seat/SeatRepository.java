package com.digitallibrary.modules.seat;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByLibraryIdOrderByRowNumberAscColNumberAsc(Long libraryId);

    Optional<Seat> findByIdAndLibraryId(Long id, Long libraryId);

    Optional<Seat> findByLibraryIdAndSeatNumber(Long libraryId, String seatNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);

    long countByLibraryIdAndStatus(Long libraryId, SeatStatus status);

    long countByLibraryId(Long libraryId);
}
