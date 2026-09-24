package com.digitallibrary.modules.payment;

import com.digitallibrary.core.errors.BadRequestException;
import com.digitallibrary.core.errors.ConflictException;
import com.digitallibrary.core.errors.ResourceNotFoundException;
import com.digitallibrary.modules.library.LibrarySettings;
import com.digitallibrary.modules.library.LibrarySettingsRepository;
import com.digitallibrary.modules.payment.dto.*;
import com.digitallibrary.modules.subscription.Subscription;
import com.digitallibrary.modules.subscription.SubscriptionRepository;
import com.digitallibrary.modules.subscription.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final LibrarySettingsRepository librarySettingsRepository;

    @Value("${app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${app.razorpay.webhook-secret}")
    private String razorpayWebhookSecret;

    public PaymentService(PaymentRepository paymentRepository,
                          SubscriptionRepository subscriptionRepository,
                          SubscriptionService subscriptionService,
                          LibrarySettingsRepository librarySettingsRepository) {
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
        this.librarySettingsRepository = librarySettingsRepository;
    }

    @Transactional
    public RazorpayOrderResponseDto createRazorpayOrder(RazorpayOrderRequestDto request, Long studentId) {
        BigDecimal amount = request.getAmount();
        Long subId = request.getSubscriptionId();

        if (subId != null) {
            Subscription subscription = subscriptionRepository.findById(subId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subId));
            amount = subscription.getAmount();
        } else if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            amount = librarySettingsRepository.findByLibraryId(request.getLibraryId())
                    .map(LibrarySettings::getMonthlyFeeAmount)
                    .orElse(new BigDecimal("700.00"));
        }

        // Generate synthetic order ID for test/prod integration
        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Payment payment = new Payment(
                request.getLibraryId(),
                studentId,
                subId,
                amount,
                PaymentMethod.ONLINE_RAZORPAY,
                PaymentStatus.PENDING
        );
        payment.setGatewayOrderId(orderId);
        payment.setIdempotencyKey(UUID.randomUUID().toString());
        payment = paymentRepository.save(payment);

        log.info("Created Razorpay payment record {} for order {} (amount: ₹{})", payment.getId(), orderId, amount);

        return new RazorpayOrderResponseDto(orderId, amount, "INR", razorpayKeyId, payment.getId());
    }

    @Transactional
    public PaymentDto verifyRazorpayPayment(PaymentVerificationRequestDto dto, Long studentId) {
        Payment payment = paymentRepository.findById(dto.getPaymentRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", dto.getPaymentRecordId()));

        if (!payment.getStudentId().equals(studentId)) {
            throw new BadRequestException("Unauthorized access to payment record", "UNAUTHORIZED_PAYMENT");
        }

        // Idempotency: If already verified, return existing record
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment {} already marked SUCCESS. Returning idempotent response.", payment.getId());
            return PaymentDto.fromEntity(payment);
        }

        // Verify Razorpay HMAC-SHA256 signature
        boolean isValidSignature = verifyHmacSha256(
                dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId(),
                dto.getRazorpaySignature(),
                razorpayKeySecret
        );

        if (!isValidSignature) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BadRequestException("Payment signature verification failed", "PAYMENT_SIGNATURE_INVALID");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId(dto.getRazorpayPaymentId());
        payment.setPaymentDate(Instant.now());
        payment = paymentRepository.save(payment);

        // Advance subscription cycle if attached
        if (payment.getSubscriptionId() != null) {
            subscriptionService.advanceSubscriptionCycle(payment.getSubscriptionId());
        }

        log.info("Payment {} verified successfully for student {}", payment.getId(), studentId);
        return PaymentDto.fromEntity(payment);
    }

    @Transactional
    public PaymentDto recordCashPayment(CashPaymentRequestDto request, String adminPhone) {
        Long subId = request.getSubscriptionId();

        if (subId == null) {
            Subscription sub = subscriptionRepository.findTopByStudentIdOrderByCreatedAtDesc(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("No subscription found for student: " + request.getStudentId()));
            subId = sub.getId();
        }

        Payment payment = new Payment(
                request.getLibraryId(),
                request.getStudentId(),
                subId,
                request.getAmount(),
                PaymentMethod.CASH,
                PaymentStatus.SUCCESS
        );
        payment.setRecordedBy("ADMIN: " + adminPhone);
        payment.setNotes(request.getNotes());
        payment.setPaymentDate(Instant.now());
        payment.setIdempotencyKey(UUID.randomUUID().toString());
        payment = paymentRepository.save(payment);

        // Automatically advance student subscription
        subscriptionService.advanceSubscriptionCycle(subId);

        log.info("Admin {} recorded cash payment of ₹{} for student {}", adminPhone, request.getAmount(), request.getStudentId());
        return PaymentDto.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getStudentPayments(Long studentId, Pageable pageable) {
        return paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId, pageable)
                .map(PaymentDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getLibraryPayments(Long libraryId, Pageable pageable) {
        return paymentRepository.findByLibraryIdOrderByPaymentDateDesc(libraryId, pageable)
                .map(PaymentDto::fromEntity);
    }

    private boolean verifyHmacSha256(String data, String signature, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hmacData = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hmacData);
            return MessageDigest.isEqual(
                    generatedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error computing HMAC-SHA256 signature", e);
            return false;
        }
    }
}
