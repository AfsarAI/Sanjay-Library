package com.digitallibrary.modules.subscription;

import com.digitallibrary.core.dto.ApiResponse;
import com.digitallibrary.core.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions & Billing", description = "Endpoints for student subscription status, renewal cycles, and admin extensions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/my-subscription")
    @Operation(summary = "Get current student subscription", description = "Returns active cycle, due date, amount, and attendance eligibility")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getMySubscription() {
        Long studentId = SecurityUtils.getCurrentUserId();
        SubscriptionDto subscription = subscriptionService.getActiveSubscription(studentId);
        return ResponseEntity.ok(ApiResponse.success(subscription));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin get student subscription", description = "Allows owner to view any student's billing status")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getStudentSubscription(@PathVariable Long studentId) {
        SubscriptionDto subscription = subscriptionService.getActiveSubscription(studentId);
        return ResponseEntity.ok(ApiResponse.success(subscription));
    }

    @PostMapping("/{subscriptionId}/extension")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin grant subscription extension", description = "Extends grace period and re-enables student attendance")
    public ResponseEntity<ApiResponse<SubscriptionDto>> grantExtension(
            @PathVariable Long subscriptionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newGraceDate,
            @RequestParam(required = false, defaultValue = "Admin granted extension") String reason) {
        SubscriptionDto updated = subscriptionService.grantAdminExtension(subscriptionId, newGraceDate, reason);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subscription extension granted successfully"));
    }
}
