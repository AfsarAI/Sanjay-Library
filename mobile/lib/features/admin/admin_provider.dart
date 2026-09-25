import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/network/api_client.dart';
import '../../core/providers/core_providers.dart';
import '../../models/admin_dashboard_model.dart';
import '../../models/admin_student_model.dart';
import '../../models/library_settings_model.dart';
import '../seats/seat_provider.dart';

final adminDashboardProvider =
    FutureProvider.autoDispose<AdminDashboardModel>((ref) async {
  final service = ref.watch(adminServiceProvider);
  return await service.getDashboard();
});

final studentDirectoryProvider = FutureProvider.autoDispose
    .family<List<AdminStudentModel>, ({String? query, String? status})>((ref, filter) async {
  final service = ref.watch(adminServiceProvider);
  return await service.getStudents(
    query: filter.query,
    status: filter.status,
  );
});

final librarySettingsProvider =
    FutureProvider.autoDispose<LibrarySettingsModel>((ref) async {
  final service = ref.watch(adminServiceProvider);
  return await service.getSettings();
});

final receptionQrTokenProvider =
    FutureProvider.autoDispose<String>((ref) async {
  final service = ref.watch(attendanceServiceProvider);
  return await service.getRotatingQrToken();
});

final adminActionProvider =
    StateNotifierProvider<AdminActionNotifier, AsyncValue<String?>>((ref) {
  return AdminActionNotifier(ref: ref);
});

class AdminActionNotifier extends StateNotifier<AsyncValue<String?>> {
  final Ref ref;

  AdminActionNotifier({required this.ref}) : super(const AsyncValue.data(null));

  Future<bool> releaseSeat(int seatId, {String reason = 'Released by owner'}) async {
    state = const AsyncValue.loading();
    try {
      final service = ref.read(adminServiceProvider);
      await service.releaseSeat(seatId: seatId, reason: reason);
      state = const AsyncValue.data('Seat released successfully');
      ref.invalidate(adminDashboardProvider);
      ref.invalidate(seatLayoutProvider);
      return true;
    } catch (e, stack) {
      if (e is ApiException) {
        state = AsyncValue.error(e.message, stack);
      } else {
        state = AsyncValue.error('Failed to release seat', stack);
      }
      return false;
    }
  }

  Future<bool> recordCashPayment({
    required int studentId,
    required double amount,
    int? subscriptionId,
    String? notes,
  }) async {
    state = const AsyncValue.loading();
    try {
      final service = ref.read(paymentServiceProvider);
      await service.recordCashPayment(
        studentId: studentId,
        amount: amount,
        subscriptionId: subscriptionId,
        notes: notes,
      );
      state = const AsyncValue.data('Cash payment recorded successfully');
      ref.invalidate(adminDashboardProvider);
      return true;
    } catch (e, stack) {
      if (e is ApiException) {
        state = AsyncValue.error(e.message, stack);
      } else {
        state = AsyncValue.error('Failed to record cash payment', stack);
      }
      return false;
    }
  }

  Future<bool> grantExtension({
    required int subscriptionId,
    required DateTime newGraceDate,
    String reason = 'Admin granted extension',
  }) async {
    state = const AsyncValue.loading();
    try {
      final service = ref.read(subscriptionServiceProvider);
      await service.grantExtension(
        subscriptionId: subscriptionId,
        newGraceDate: newGraceDate,
        reason: reason,
      );
      state = const AsyncValue.data('Extension granted successfully');
      ref.invalidate(adminDashboardProvider);
      return true;
    } catch (e, stack) {
      if (e is ApiException) {
        state = AsyncValue.error(e.message, stack);
      } else {
        state = AsyncValue.error('Failed to grant extension', stack);
      }
      return false;
    }
  }
}
