package com.digitallibrary.modules.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Page<Payment> findByStudentIdOrderByPaymentDateDesc(Long studentId, Pageable pageable);

    Page<Payment> findByLibraryIdOrderByPaymentDateDesc(Long libraryId, Pageable pageable);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.libraryId = :libraryId AND p.status = 'SUCCESS' AND p.paymentDate >= :since")
    BigDecimal sumSuccessfulRevenueSince(@Param("libraryId") Long libraryId, @Param("since") Instant since);

    java.util.List<Payment> findByLibraryIdOrderByPaymentDateDesc(Long libraryId);

    java.util.List<Payment> findByLibraryIdAndPaymentDateBetweenOrderByPaymentDateDesc(Long libraryId, Instant startDate, Instant endDate);
}
