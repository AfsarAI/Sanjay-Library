package com.digitallibrary.modules.attendance;

import com.digitallibrary.core.dto.ApiResponse;
import com.digitallibrary.core.security.SecurityUtils;
import com.digitallibrary.modules.attendance.dto.AttendanceCheckInRequestDto;
import com.digitallibrary.modules.attendance.dto.AttendanceQrTokenResponseDto;
import com.digitallibrary.modules.attendance.dto.AttendanceRecordDto;
import com.digitallibrary.modules.attendance.dto.TodayAttendanceSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance", description = "Endpoints for rotating QR code attendance, check-in, check-out, and daily occupancy")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/qr-token")
    @Operation(summary = "Generate rotating QR token", description = "Used by in-library tablet/screen to fetch 30-second rotating cryptographic QR token")
    public ResponseEntity<ApiResponse<AttendanceQrTokenResponseDto>> getRotatingQrToken(
            @RequestParam(defaultValue = "1") Long libraryId) {
        AttendanceQrTokenResponseDto token = attendanceService.generateRotatingQrToken(libraryId);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @PostMapping("/check-in")
    @Operation(summary = "Student Check-In via QR scanner", description = "Verifies dynamic QR token and subscription validity before recording arrival time")
    public ResponseEntity<ApiResponse<AttendanceRecordDto>> checkIn(
            @Valid @RequestBody AttendanceCheckInRequestDto request) {
        Long studentId = SecurityUtils.getCurrentUserId();
        AttendanceRecordDto record = attendanceService.checkInWithQr(studentId, request.getQrToken());
        return ResponseEntity.ok(ApiResponse.success(record, "Checked in successfully"));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Student Check-Out", description = "Closes active session, computes duration, and updates departure time")
    public ResponseEntity<ApiResponse<AttendanceRecordDto>> checkOut() {
        Long studentId = SecurityUtils.getCurrentUserId();
        AttendanceRecordDto record = attendanceService.checkOut(studentId);
        return ResponseEntity.ok(ApiResponse.success(record, "Checked out successfully"));
    }

    @GetMapping("/my-history")
    @Operation(summary = "Get student attendance logs", description = "Returns paginated list of student's past check-in and checkout history")
    public ResponseEntity<ApiResponse<Page<AttendanceRecordDto>>> getMyAttendance(
            @PageableDefault(size = 30, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        Long studentId = SecurityUtils.getCurrentUserId();
        Page<AttendanceRecordDto> history = attendanceService.getStudentAttendanceHistory(studentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/today-summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin Today Attendance Summary", description = "Provides real-time headcounts: inside library, checked out, absent, and total capacity")
    public ResponseEntity<ApiResponse<TodayAttendanceSummaryDto>> getTodaySummary(
            @RequestParam(defaultValue = "1") Long libraryId) {
        TodayAttendanceSummaryDto summary = attendanceService.getTodaySummary(libraryId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
