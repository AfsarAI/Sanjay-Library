package com.digitallibrary.modules.admin;

import com.digitallibrary.modules.attendance.AttendanceRecord;
import com.digitallibrary.modules.attendance.AttendanceRepository;
import com.digitallibrary.modules.payment.Payment;
import com.digitallibrary.modules.payment.PaymentRepository;
import com.digitallibrary.modules.seat.Seat;
import com.digitallibrary.modules.seat.SeatRepository;
import com.digitallibrary.modules.user.User;
import com.digitallibrary.modules.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of("Asia/Kolkata"));

    public ReportService(AttendanceRepository attendanceRepository,
                         PaymentRepository paymentRepository,
                         UserRepository userRepository,
                         SeatRepository seatRepository) {
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional(readOnly = true)
    public String generateAttendanceCsv(Long libraryId, LocalDate startDate, LocalDate endDate) {
        StringWriter writer = new StringWriter();
        writer.append("Record ID,Date,Student Name,Phone Number,Seat Number,Check-In Time (IST),Check-Out Time (IST),Duration (Min),Status,Method,Notes\n");

        List<AttendanceRecord> records = attendanceRepository.findByLibraryIdAndDateBetweenOrderByDateDesc(libraryId, startDate, endDate);
        for (AttendanceRecord r : records) {
            User student = userRepository.findById(r.getStudentId()).orElse(null);
            Seat seat = seatRepository.findById(r.getSeatId()).orElse(null);

            String studentName = student != null ? student.getFullName() : "N/A";
            String studentPhone = student != null ? student.getPhoneNumber() : "N/A";
            String seatNum = seat != null ? seat.getSeatNumber() : "N/A";
            String inTime = r.getCheckInTime() != null ? TIME_FMT.format(r.getCheckInTime()) : "";
            String outTime = r.getCheckOutTime() != null ? TIME_FMT.format(r.getCheckOutTime()) : "";

            writer.append(String.format("%d,%s,\"%s\",%s,%s,%s,%s,%s,%s,%s,\"%s\"\n",
                    r.getId(),
                    r.getDate().format(DATE_FMT),
                    escapeCsv(studentName),
                    studentPhone,
                    seatNum,
                    inTime,
                    outTime,
                    r.getDurationMinutes() != null ? r.getDurationMinutes().toString() : "0",
                    r.getStatus().name(),
                    r.getVerificationMethod().name(),
                    escapeCsv(r.getNotes() != null ? r.getNotes() : "")
            ));
        }

        return writer.toString();
    }

    @Transactional(readOnly = true)
    public String generateRevenueCsv(Long libraryId, Instant startDate, Instant endDate) {
        StringWriter writer = new StringWriter();
        writer.append("Payment ID,Payment Date (IST),Student Name,Phone Number,Amount (INR),Payment Method,Gateway Order ID,Gateway Payment ID,Status,Recorded By\n");

        List<Payment> payments = paymentRepository.findByLibraryIdAndPaymentDateBetweenOrderByPaymentDateDesc(libraryId, startDate, endDate);
        for (Payment p : payments) {
            User student = userRepository.findById(p.getStudentId()).orElse(null);
            String studentName = student != null ? student.getFullName() : "N/A";
            String studentPhone = student != null ? student.getPhoneNumber() : "N/A";
            String payDate = p.getPaymentDate() != null ? TIME_FMT.format(p.getPaymentDate()) : "";

            writer.append(String.format("%d,%s,\"%s\",%s,%.2f,%s,%s,%s,%s,\"%s\"\n",
                    p.getId(),
                    payDate,
                    escapeCsv(studentName),
                    studentPhone,
                    p.getAmount(),
                    p.getMethod().name(),
                    p.getGatewayOrderId() != null ? p.getGatewayOrderId() : "",
                    p.getGatewayPaymentId() != null ? p.getGatewayPaymentId() : "",
                    p.getStatus().name(),
                    escapeCsv(p.getRecordedBy() != null ? p.getRecordedBy() : "ONLINE")
            ));
        }

        return writer.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
