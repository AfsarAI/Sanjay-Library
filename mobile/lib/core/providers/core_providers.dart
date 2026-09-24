import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../network/api_client.dart';
import '../security/token_storage.dart';
import '../../services/admin_service.dart';
import '../../services/admission_service.dart';
import '../../services/attendance_service.dart';
import '../../services/auth_service.dart';
import '../../services/payment_service.dart';
import '../../services/seat_service.dart';
import '../../services/subscription_service.dart';

final tokenStorageProvider = Provider<TokenStorage>((ref) {
  return TokenStorage();
});

final apiClientProvider = Provider<ApiClient>((ref) {
  final tokenStorage = ref.watch(tokenStorageProvider);
  return ApiClient(tokenStorage: tokenStorage);
});

final authServiceProvider = Provider<AuthService>((ref) {
  return AuthService(
    apiClient: ref.watch(apiClientProvider),
    tokenStorage: ref.watch(tokenStorageProvider),
  );
});

final seatServiceProvider = Provider<SeatService>((ref) {
  return SeatService(apiClient: ref.watch(apiClientProvider));
});

final admissionServiceProvider = Provider<AdmissionService>((ref) {
  return AdmissionService(apiClient: ref.watch(apiClientProvider));
});

final attendanceServiceProvider = Provider<AttendanceService>((ref) {
  return AttendanceService(apiClient: ref.watch(apiClientProvider));
});

final subscriptionServiceProvider = Provider<SubscriptionService>((ref) {
  return SubscriptionService(apiClient: ref.watch(apiClientProvider));
});

final paymentServiceProvider = Provider<PaymentService>((ref) {
  return PaymentService(apiClient: ref.watch(apiClientProvider));
});

final adminServiceProvider = Provider<AdminService>((ref) {
  return AdminService(apiClient: ref.watch(apiClientProvider));
});
