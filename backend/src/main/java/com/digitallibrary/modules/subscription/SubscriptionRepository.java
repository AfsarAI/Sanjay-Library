package com.digitallibrary.modules.subscription;

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
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findTopByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Subscription> findByLibraryIdAndStatus(Long libraryId, SubscriptionStatus status);

    Page<Subscription> findByLibraryId(Long libraryId, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.dueDate <= :today")
    List<Subscription> findActiveSubscriptionsDue(@Param("today") LocalDate today);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'OVERDUE' AND :today > s.graceUntil")
    List<Subscription> findOverdueSubscriptionsPastGrace(@Param("today") LocalDate today);

    long countByLibraryIdAndStatus(Long libraryId, SubscriptionStatus status);
}
