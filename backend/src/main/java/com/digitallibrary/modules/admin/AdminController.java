package com.digitallibrary.modules.admin;

import com.digitallibrary.core.dto.ApiResponse;
import com.digitallibrary.core.security.SecurityUtils;
import com.digitallibrary.modules.admin.dto.AdminDashboardDto;
import com.digitallibrary.modules.admin.dto.AdminStudentDirectoryDto;
import com.digitallibrary.modules.user.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Operations", description = "Endpoints for library owner dashboard, student directory, reports, seat release, and controls")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;

    public AdminController(AdminService adminService, ReportService reportService) {
        this.adminService = adminService;
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get full owner dashboard metrics", description = "Returns seat breakdown, live inside headcount, overdue alerts, and revenue")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboard(
            @RequestParam(defaultValue = "1") Long libraryId) {
        AdminDashboardDto dashboard = adminService.getDashboard(libraryId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @PostMapping("/students/{studentId}/change-seat")
    @Operation(summary = "Admin change student seat", description = "Swaps student's current seat with a new available seat and updates admission record")
    public ResponseEntity<ApiResponse<Void>> changeSeat(
            @PathVariable Long studentId,
            @RequestParam Long newSeatId,
            @RequestParam(defaultValue = "1") Long libraryId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        adminService.changeStudentSeat(libraryId, studentId, newSeatId, adminId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student seat changed successfully"));
    }

    @PostMapping("/seats/{seatId}/release")
    @Operation(summary = "Admin release seat", description = "Vacates an occupied seat, releases admission, and returns seat to AVAILABLE")
    public ResponseEntity<ApiResponse<Void>> releaseSeat(
            @PathVariable Long seatId,
            @RequestParam(defaultValue = "1") Long libraryId,
            @RequestParam(defaultValue = "Released by owner") String reason) {
        Long adminId = SecurityUtils.getCurrentUserId();
        adminService.releaseSeat(libraryId, seatId, adminId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Seat released successfully"));
    }

    @PostMapping("/students/{studentId}/status")
    @Operation(summary = "Suspend or reactivate student account", description = "Updates student account status between ACTIVE and SUSPENDED")
    public ResponseEntity<ApiResponse<Void>> updateStudentStatus(
            @PathVariable Long studentId,
            @RequestParam UserStatus status) {
        Long adminId = SecurityUtils.getCurrentUserId();
        adminService.toggleStudentStatus(studentId, status, adminId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student status updated to " + status));
    }

    @GetMapping("/students")
    @Operation(summary = "Get student directory", description = "Returns searchable student directory with seat assignments and subscription status")
    public ResponseEntity<ApiResponse<List<AdminStudentDirectoryDto>>> getStudents(
            @RequestParam(defaultValue = "1") Long libraryId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status) {
        List<AdminStudentDirectoryDto> students = adminService.getStudentDirectory(libraryId, q, status);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/reports/attendance/csv")
    @Operation(summary = "Export attendance register CSV", description = "Downloads detailed attendance records for specified date range in CSV format")
    public ResponseEntity<byte[]> downloadAttendanceReport(
            @RequestParam(defaultValue = "1") Long libraryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        String csvData = reportService.generateAttendanceCsv(libraryId, startDate, endDate);
        String filename = "sanjay_library_attendance_" + startDate + "_to_" + endDate + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @GetMapping("/reports/revenue/csv")
    @Operation(summary = "Export revenue register CSV", description = "Downloads all payment ledger transactions in CSV format")
    public ResponseEntity<byte[]> downloadRevenueReport(
            @RequestParam(defaultValue = "1") Long libraryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        if (startDate == null) startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        if (endDate == null) endDate = Instant.now();

        String csvData = reportService.generateRevenueCsv(libraryId, startDate, endDate);
        String filename = "sanjay_library_revenue_" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
