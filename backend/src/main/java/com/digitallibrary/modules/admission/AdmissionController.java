package com.digitallibrary.modules.admission;

import com.digitallibrary.core.dto.ApiResponse;
import com.digitallibrary.core.security.SecurityUtils;
import com.digitallibrary.modules.admission.dto.AdmissionRequestDto;
import com.digitallibrary.modules.admission.dto.AdmissionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admissions")
@Tag(name = "Admissions", description = "Endpoints for new student admission, seat assignment, and enrollment details")
public class AdmissionController {

    private final AdmissionService admissionService;

    public AdmissionController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @PostMapping
    @Operation(summary = "Submit new admission", description = "Assigns chosen seat, confirms enrollment, and creates initial subscription cycle")
    public ResponseEntity<ApiResponse<AdmissionResponseDto>> submitAdmission(
            @Valid @RequestBody AdmissionRequestDto request) {
        Long studentId = SecurityUtils.getCurrentUserId();
        AdmissionResponseDto response = admissionService.processAdmission(request, studentId);
        return new ResponseEntity<>(ApiResponse.success(response, "Admission processed successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/my-admission")
    @Operation(summary = "Get current student admission details", description = "Retrieves active admission, assigned seat, and joining date")
    public ResponseEntity<ApiResponse<AdmissionResponseDto>> getMyAdmission() {
        Long studentId = SecurityUtils.getCurrentUserId();
        AdmissionResponseDto response = admissionService.getMyAdmission(studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{admissionId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin cancel admission", description = "Cancels admission and immediately releases assigned seat to AVAILABLE")
    public ResponseEntity<ApiResponse<Void>> cancelAdmission(
            @PathVariable Long admissionId,
            @RequestParam(defaultValue = "Cancelled by admin") String reason) {
        admissionService.cancelAdmission(admissionId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Admission cancelled and seat released successfully"));
    }
}
