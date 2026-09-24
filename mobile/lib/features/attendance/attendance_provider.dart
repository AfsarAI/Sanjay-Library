import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/network/api_client.dart';
import '../../core/providers/core_providers.dart';
import '../../models/attendance_model.dart';

final attendanceHistoryProvider =
    FutureProvider.autoDispose<List<AttendanceModel>>((ref) async {
  final service = ref.watch(attendanceServiceProvider);
  return await service.getMyAttendanceHistory();
});

final activeAttendanceProvider =
    StateNotifierProvider<ActiveAttendanceNotifier, AsyncValue<AttendanceModel?>>((ref) {
  return ActiveAttendanceNotifier(ref: ref);
});

class ActiveAttendanceNotifier extends StateNotifier<AsyncValue<AttendanceModel?>> {
  final Ref ref;

  ActiveAttendanceNotifier({required this.ref}) : super(const AsyncValue.data(null)) {
    loadLatestAttendance();
  }

  Future<void> loadLatestAttendance() async {
    try {
      final service = ref.read(attendanceServiceProvider);
      final history = await service.getMyAttendanceHistory(size: 1);
      if (history.isNotEmpty && history.first.isCurrentlyCheckedIn) {
        state = AsyncValue.data(history.first);
      } else {
        state = const AsyncValue.data(null);
      }
    } catch (_) {
      state = const AsyncValue.data(null);
    }
  }

  Future<bool> checkIn(String qrToken) async {
    state = const AsyncValue.loading();
    try {
      final service = ref.read(attendanceServiceProvider);
      final record = await service.checkIn(qrToken);
      state = AsyncValue.data(record);
      ref.invalidate(attendanceHistoryProvider);
      return true;
    } catch (e, stack) {
      if (e is ApiException) {
        state = AsyncValue.error(e.message, stack);
      } else {
        state = AsyncValue.error('Check-in failed. Please try again.', stack);
      }
      return false;
    }
  }

  Future<bool> checkOut() async {
    state = const AsyncValue.loading();
    try {
      final service = ref.read(attendanceServiceProvider);
      await service.checkOut();
      state = const AsyncValue.data(null);
      ref.invalidate(attendanceHistoryProvider);
      return true;
    } catch (e, stack) {
      if (e is ApiException) {
        state = AsyncValue.error(e.message, stack);
      } else {
        state = AsyncValue.error('Checkout failed. Please try again.', stack);
      }
      return false;
    }
  }
}
