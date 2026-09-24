package com.digitallibrary.modules.payment;

import com.digitallibrary.core.dto.ApiResponse;
import com.digitallibrary.core.security.SecurityUtils;
import com.digitallibrary.modules.payment.dto.*;
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
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Endpoints for online Razorpay payments, signature verification, and cash ledger")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    @Operation(summary = "Create Razorpay payment order", description = "Initializes an online payment session and returns order details for native checkout")
    public ResponseEntity<ApiResponse<RazorpayOrderResponseDto>> createOrder(
            @Valid @RequestBody RazorpayOrderRequestDto request) {
        Long studentId = SecurityUtils.getCurrentUserId();
        RazorpayOrderResponseDto order = paymentService.createRazorpayOrder(request, studentId);
        return ResponseEntity.ok(ApiResponse.success(order, "Payment order created successfully"));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment signature", description = "Server-side cryptographic verification of HMAC-SHA256 signature to prevent client fraud")
    public ResponseEntity<ApiResponse<PaymentDto>> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequestDto request) {
        Long studentId = SecurityUtils.getCurrentUserId();
        PaymentDto payment = paymentService.verifyRazorpayPayment(request, studentId);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment verified and subscription extended successfully"));
    }

    @PostMapping("/cash")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Record offline cash payment (Admin/Staff only)", description = "Logs cash received by owner, creates payment record, and automatically advances student subscription")
    public ResponseEntity<ApiResponse<PaymentDto>> recordCashPayment(
            @Valid @RequestBody CashPaymentRequestDto request) {
        String adminPhone = SecurityUtils.getCurrentUser().getUsername();
        PaymentDto payment = paymentService.recordCashPayment(request, adminPhone);
        return ResponseEntity.ok(ApiResponse.success(payment, "Cash payment recorded and subscription extended successfully"));
    }

    @GetMapping("/my-history")
    @Operation(summary = "Get student payment history", description = "Returns paginated list of student's past payments and digital receipts")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getMyPaymentHistory(
            @PageableDefault(size = 20, sort = "paymentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Long studentId = SecurityUtils.getCurrentUserId();
        Page<PaymentDto> history = paymentService.getStudentPayments(studentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/library")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin get all library payments", description = "Returns paginated ledger of all online and cash payments for the branch")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getLibraryPayments(
            @RequestParam(defaultValue = "1") Long libraryId,
            @PageableDefault(size = 20, sort = "paymentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentDto> ledger = paymentService.getLibraryPayments(libraryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(ledger));
    }
}
