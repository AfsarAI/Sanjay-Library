package com.digitallibrary.modules.attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByStudentIdAndDate(Long studentId, LocalDate date);

    Page<AttendanceRecord> findByStudentIdOrderByDateDesc(Long studentId, Pageable pageable);

    List<AttendanceRecord> findByLibraryIdAndDate(Long libraryId, LocalDate date);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.libraryId = :libraryId AND a.date = :date AND a.checkOutTime IS NULL")
    long countCurrentlyInside(@Param("libraryId") Long libraryId, @Param("date") LocalDate date);

    long countByLibraryIdAndDate(Long libraryId, LocalDate date);
}
