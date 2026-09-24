package com.digitallibrary.modules.payment;

import com.digitallibrary.core.errors.BadRequestException;
import com.digitallibrary.modules.library.LibrarySettingsRepository;
import com.digitallibrary.modules.payment.dto.CashPaymentRequestDto;
import com.digitallibrary.modules.payment.dto.PaymentDto;
import com.digitallibrary.modules.payment.dto.PaymentVerificationRequestDto;
import com.digitallibrary.modules.payment.dto.RazorpayOrderRequestDto;
import com.digitallibrary.modules.payment.dto.RazorpayOrderResponseDto;
import com.digitallibrary.modules.subscription.Subscription;
import com.digitallibrary.modules.subscription.SubscriptionRepository;
import com.digitallibrary.modules.subscription.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private LibrarySettingsRepository librarySettingsRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                subscriptionRepository,
                subscriptionService,
                librarySettingsRepository
        );
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "rzp_test_key");
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", "rzp_test_secret");
        ReflectionTestUtils.setField(paymentService, "razorpayWebhookSecret", "rzp_test_webhook_secret");
    }

    @Test
    @DisplayName("Should successfully create Razorpay order")
    void createRazorpayOrder_Success() {
        RazorpayOrderRequestDto request = new RazorpayOrderRequestDto();
        request.setLibraryId(1L);
        request.setAmount(new BigDecimal("700.00"));

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(101L);
            return p;
        });

        RazorpayOrderResponseDto response = paymentService.createRazorpayOrder(request, 100L);

        assertNotNull(response);
        assertTrue(response.getOrderId().startsWith("order_"));
        assertEquals(new BigDecimal("700.00"), response.getAmount());
        assertEquals("rzp_test_key", response.getKeyId());
        assertEquals(101L, response.getPaymentRecordId());
    }

    @Test
    @DisplayName("Should successfully record cash payment and advance subscription")
    void recordCashPayment_Success() {
        CashPaymentRequestDto request = new CashPaymentRequestDto();
        request.setLibraryId(1L);
        request.setStudentId(100L);
        request.setSubscriptionId(50L);
        request.setAmount(new BigDecimal("700.00"));
        request.setNotes("Cash received in library desk");

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(201L);
            return p;
        });

        PaymentDto dto = paymentService.recordCashPayment(request, "9876543210");

        assertNotNull(dto);
        assertEquals(PaymentMethod.CASH, dto.getMethod());
        assertEquals(PaymentStatus.SUCCESS, dto.getStatus());
        assertEquals(new BigDecimal("700.00"), dto.getAmount());
        verify(subscriptionService).advanceSubscriptionCycle(50L);
    }

    @Test
    @DisplayName("Should return existing payment idempotently if already verified SUCCESS")
    void verifyPayment_IdempotentDuplicate_ReturnsExistingWithoutReAdvancing() {
        Payment payment = new Payment(1L, 100L, 50L, new BigDecimal("700.00"), PaymentMethod.ONLINE_RAZORPAY, PaymentStatus.SUCCESS);
        payment.setId(301L);

        when(paymentRepository.findById(301L)).thenReturn(Optional.of(payment));

        PaymentVerificationRequestDto request = new PaymentVerificationRequestDto();
        request.setPaymentRecordId(301L);
        request.setRazorpayOrderId("order_123");
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpaySignature("sig_123");

        PaymentDto dto = paymentService.verifyRazorpayPayment(request, 100L);

        assertNotNull(dto);
        assertEquals(PaymentStatus.SUCCESS, dto.getStatus());
        verify(subscriptionService, never()).advanceSubscriptionCycle(anyLong());
    }

    @Test
    @DisplayName("Should throw BadRequestException if signature is invalid")
    void verifyPayment_InvalidSignature_ThrowsBadRequest() {
        Payment payment = new Payment(1L, 100L, 50L, new BigDecimal("700.00"), PaymentMethod.ONLINE_RAZORPAY, PaymentStatus.PENDING);
        payment.setId(301L);

        when(paymentRepository.findById(301L)).thenReturn(Optional.of(payment));

        PaymentVerificationRequestDto request = new PaymentVerificationRequestDto();
        request.setPaymentRecordId(301L);
        request.setRazorpayOrderId("order_123");
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpaySignature("invalid_tampered_signature");

        assertThrows(BadRequestException.class, () -> paymentService.verifyRazorpayPayment(request, 100L));
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }
}
