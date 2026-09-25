package com.digitallibrary.modules.admin;

import com.digitallibrary.modules.attendance.AttendanceRecord;
import com.digitallibrary.modules.attendance.AttendanceRepository;
import com.digitallibrary.modules.attendance.AttendanceVerificationMethod;
import com.digitallibrary.modules.payment.Payment;
import com.digitallibrary.modules.payment.PaymentMethod;
import com.digitallibrary.modules.payment.PaymentRepository;
import com.digitallibrary.modules.payment.PaymentStatus;
import com.digitallibrary.modules.seat.Seat;
import com.digitallibrary.modules.seat.SeatRepository;
import com.digitallibrary.modules.user.User;
import com.digitallibrary.modules.user.UserRepository;
import com.digitallibrary.modules.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SeatRepository seatRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(
                attendanceRepository,
                paymentRepository,
                userRepository,
                seatRepository
        );
    }

    @Test
    @DisplayName("Should generate valid attendance CSV register")
    void generateAttendanceCsv_Success() {
        AttendanceRecord record = new AttendanceRecord(1L, 100L, 10L, LocalDate.now(), Instant.now().minus(2, ChronoUnit.HOURS), AttendanceVerificationMethod.QR_ROTATING);
        record.setId(555L);
        record.setDurationMinutes(120);

        User student = new User(1L, "9876543210", "student@example.com", "hash", "Amit Verma", UserRole.ROLE_STUDENT);
        Seat seat = new Seat(1L, "A10", 1, 10, com.digitallibrary.modules.seat.SeatStatus.OCCUPIED);

        when(attendanceRepository.findByLibraryIdAndDateBetweenOrderByDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of(record));
        when(userRepository.findById(100L)).thenReturn(Optional.of(student));
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));

        String csv = reportService.generateAttendanceCsv(1L, LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(csv);
        assertTrue(csv.contains("Record ID,Date,Student Name"));
        assertTrue(csv.contains("Amit Verma"));
        assertTrue(csv.contains("A10"));
        assertTrue(csv.contains("9876543210"));
    }

    @Test
    @DisplayName("Should generate valid revenue CSV register")
    void generateRevenueCsv_Success() {
        Payment payment = new Payment(1L, 100L, 50L, new BigDecimal("700.00"), PaymentMethod.ONLINE_RAZORPAY, PaymentStatus.SUCCESS);
        payment.setId(888L);
        payment.setPaymentDate(Instant.now());
        payment.setGatewayOrderId("order_xyz");
        payment.setGatewayPaymentId("pay_xyz");

        User student = new User(1L, "9876543210", "student@example.com", "hash", "Amit Verma", UserRole.ROLE_STUDENT);

        when(paymentRepository.findByLibraryIdAndPaymentDateBetweenOrderByPaymentDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of(payment));
        when(userRepository.findById(100L)).thenReturn(Optional.of(student));

        String csv = reportService.generateRevenueCsv(1L, Instant.now().minus(30, ChronoUnit.DAYS), Instant.now());

        assertNotNull(csv);
        assertTrue(csv.contains("Payment ID,Payment Date (IST),Student Name"));
        assertTrue(csv.contains("Amit Verma"));
        assertTrue(csv.contains("700.00"));
        assertTrue(csv.contains("ONLINE_RAZORPAY"));
    }
}
