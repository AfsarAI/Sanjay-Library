package com.digitallibrary.modules.admission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, Long> {

    Optional<Admission> findTopByStudentIdAndStatus(Long studentId, AdmissionStatus status);

    Optional<Admission> findTopBySeatIdAndStatus(Long seatId, AdmissionStatus status);

    Page<Admission> findByLibraryIdAndStatus(Long libraryId, AdmissionStatus status, Pageable pageable);

    long countByLibraryIdAndStatus(Long libraryId, AdmissionStatus status);
}
