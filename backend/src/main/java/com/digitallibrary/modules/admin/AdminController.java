package com.digitallibrary.modules.admin;

import com.digitallibrary.core.dto.ApiResponse;
import com.digitallibrary.core.security.SecurityUtils;
import com.digitallibrary.modules.admin.dto.AdminDashboardDto;
import com.digitallibrary.modules.user.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@Tag(name = "Admin Operations", description = "Endpoints for library owner dashboard, seat release, overrides, and student status control")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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
}
