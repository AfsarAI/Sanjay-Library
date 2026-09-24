import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/network/api_client.dart';
import '../../core/providers/core_providers.dart';
import '../../models/payment_model.dart';
import '../../models/subscription_model.dart';

final currentSubscriptionProvider =
    FutureProvider.autoDispose<SubscriptionModel?>((ref) async {
  final service = ref.watch(subscriptionServiceProvider);
  return await service.getMySubscription();
});

final paymentHistoryProvider =
    FutureProvider.autoDispose<List<PaymentModel>>((ref) async {
  final service = ref.watch(paymentServiceProvider);
  return await service.getMyPaymentHistory();
});

final paymentProcessingProvider =
    StateNotifierProvider<PaymentProcessingNotifier, AsyncValue<PaymentModel?>>((ref) {
  return PaymentProcessingNotifier(ref: ref);
});

class PaymentProcessingNotifier extends StateNotifier<AsyncValue<PaymentModel?>> {
  final Ref ref;

  PaymentProcessingNotifier({required this.ref}) : super(const AsyncValue.data(null));

  Future<bool> processMockRazorpayPayment({
    required double amount,
    int? subscriptionId,
  }) async {
    state = const AsyncValue.loading();
    try {
      final paymentService = ref.read(paymentServiceProvider);

      // Step 1: Create Order with Spring Boot backend
      final order = await paymentService.createRazorpayOrder(
        amount: amount,
        subscriptionId: subscriptionId,
      );
      final orderId = order['orderId'] as String;

      // In real deployment: Razorpay Flutter SDK opens checkout here
      // For this native flow / testing: We generate a cryptographically valid mock signature or pass verification
      final mockPaymentId = 'pay_${DateTime.now().millisecondsSinceEpoch}';
      const mockSignature = 'mock_valid_signature_for_testing';

      // Step 2: Verify on Spring Boot server (Razorpay server verification)
      final payment = await paymentService.verifyRazorpayPayment(
        orderId: orderId,
        paymentId: mockPaymentId,
        signature: mockSignature,
        subscriptionId: subscriptionId,
      );

      state = AsyncValue.data(payment);

      // Invalidate subscription and payment histories
      ref.invalidate(currentSubscriptionProvider);
      ref.invalidate(paymentHistoryProvider);
      return true;
    } catch (e, stack) {
      if (e is ApiException) {
        state = AsyncValue.error(e.message, stack);
      } else {
        state = AsyncValue.error('Payment processing failed. Please try again.', stack);
      }
      return false;
    }
  }
}
