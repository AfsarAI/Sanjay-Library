import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/network/api_client.dart';
import '../../core/providers/core_providers.dart';
import '../../models/seat_model.dart';

final seatLayoutProvider = FutureProvider.autoDispose<SeatLayoutModel>((ref) async {
  final seatService = ref.watch(seatServiceProvider);
  return await seatService.getSeatLayout();
});

final selectedSeatProvider = StateProvider<SeatModel?>((ref) => null);

final reservationNotifierProvider =
    StateNotifierProvider<ReservationNotifier, AsyncValue<Map<String, dynamic>?>>((ref) {
  return ReservationNotifier(ref: ref);
});

class ReservationNotifier extends StateNotifier<AsyncValue<Map<String, dynamic>?>> {
  final Ref ref;

  ReservationNotifier({required this.ref}) : super(const AsyncValue.data(null));

  Future<bool> reserveSeat(int seatId) async {
    state = const AsyncValue.loading();
    try {
      final seatService = ref.read(seatServiceProvider);
      final result = await seatService.reserveSeat(seatId: seatId);
      state = AsyncValue.data(result);
      // Invalidate layout to show newly reserved seat immediately
      ref.invalidate(seatLayoutProvider);
      return true;
    } catch (e, stack) {
      if (e is ApiException) {
        state = AsyncValue.error(e.message, stack);
      } else {
        state = AsyncValue.error('Unable to reserve seat. Please try another.', stack);
      }
      return false;
    }
  }
}
