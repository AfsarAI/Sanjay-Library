import '../core/config/app_config.dart';
import '../core/network/api_client.dart';
import '../models/attendance_model.dart';

class AttendanceService {
  final ApiClient apiClient;

  AttendanceService({required this.apiClient});

  Future<String> getRotatingQrToken([int libraryId = AppConfig.defaultLibraryId]) async {
    try {
      final response = await apiClient.dio.get(
        '/attendance/qr-token',
        queryParameters: {'libraryId': libraryId},
      );
      final data = response.data['data'] as Map<String, dynamic>;
      return data['qrToken'] as String;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<AttendanceModel> checkIn(String qrToken) async {
    try {
      final response = await apiClient.dio.post(
        '/attendance/check-in',
        data: {'qrToken': qrToken.trim()},
      );
      final data = response.data['data'] as Map<String, dynamic>;
      return AttendanceModel.fromJson(data);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<AttendanceModel> checkOut() async {
    try {
      final response = await apiClient.dio.post('/attendance/check-out');
      final data = response.data['data'] as Map<String, dynamic>;
      return AttendanceModel.fromJson(data);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<List<AttendanceModel>> getMyAttendanceHistory({int page = 0, int size = 30}) async {
    try {
      final response = await apiClient.dio.get(
        '/attendance/my-history',
        queryParameters: {'page': page, 'size': size},
      );
      final pageData = response.data['data'] as Map<String, dynamic>;
      final content = pageData['content'] as List<dynamic>? ?? [];
      return content.map((e) => AttendanceModel.fromJson(e as Map<String, dynamic>)).toList();
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<Map<String, dynamic>> getTodaySummary([int libraryId = AppConfig.defaultLibraryId]) async {
    try {
      final response = await apiClient.dio.get(
        '/attendance/today-summary',
        queryParameters: {'libraryId': libraryId},
      );
      return response.data['data'] as Map<String, dynamic>;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }
}
