import '../core/config/app_config.dart';
import '../core/network/api_client.dart';
import '../models/admin_dashboard_model.dart';

class AdminService {
  final ApiClient apiClient;

  AdminService({required this.apiClient});

  Future<AdminDashboardModel> getDashboard([int libraryId = AppConfig.defaultLibraryId]) async {
    try {
      final response = await apiClient.dio.get(
        '/admin/dashboard',
        queryParameters: {'libraryId': libraryId},
      );
      final data = response.data['data'] as Map<String, dynamic>;
      return AdminDashboardModel.fromJson(data);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<void> changeStudentSeat({
    required int studentId,
    required int newSeatId,
    int libraryId = AppConfig.defaultLibraryId,
  }) async {
    try {
      await apiClient.dio.post(
        '/admin/students/$studentId/change-seat',
        queryParameters: {
          'newSeatId': newSeatId,
          'libraryId': libraryId,
        },
      );
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<void> releaseSeat({
    required int seatId,
    int libraryId = AppConfig.defaultLibraryId,
    String reason = 'Released by owner',
  }) async {
    try {
      await apiClient.dio.post(
        '/admin/seats/$seatId/release',
        queryParameters: {
          'libraryId': libraryId,
          'reason': reason,
        },
      );
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<void> updateStudentStatus({
    required int studentId,
    required String status,
  }) async {
    try {
      await apiClient.dio.post(
        '/admin/students/$studentId/status',
        queryParameters: {'status': status},
      );
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }
}
