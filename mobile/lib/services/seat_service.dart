import '../core/config/app_config.dart';
import '../core/network/api_client.dart';
import '../models/seat_model.dart';

class SeatService {
  final ApiClient apiClient;

  SeatService({required this.apiClient});

  Future<SeatLayoutModel> getSeatLayout([int libraryId = AppConfig.defaultLibraryId]) async {
    try {
      final response = await apiClient.dio.get(
        '/seats/layout',
        queryParameters: {'libraryId': libraryId},
      );

      final data = response.data['data'] as Map<String, dynamic>;
      return SeatLayoutModel.fromJson(data);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<Map<String, dynamic>> reserveSeat({
    required int seatId,
    int libraryId = AppConfig.defaultLibraryId,
  }) async {
    try {
      final response = await apiClient.dio.post(
        '/seats/$seatId/reserve',
        queryParameters: {'libraryId': libraryId},
      );

      return response.data['data'] as Map<String, dynamic>;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }
}
