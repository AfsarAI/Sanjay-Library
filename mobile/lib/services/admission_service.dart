import 'package:intl/intl.dart';
import '../core/config/app_config.dart';
import '../core/network/api_client.dart';

class AdmissionService {
  final ApiClient apiClient;

  AdmissionService({required this.apiClient});

  Future<Map<String, dynamic>> submitAdmission({
    required int seatId,
    required DateTime joiningDate,
    String? emergencyContact,
    int libraryId = AppConfig.defaultLibraryId,
  }) async {
    try {
      final dateStr = DateFormat('yyyy-MM-dd').format(joiningDate);
      final response = await apiClient.dio.post(
        '/admissions',
        data: {
          'libraryId': libraryId,
          'seatId': seatId,
          'joiningDate': dateStr,
          if (emergencyContact != null && emergencyContact.isNotEmpty)
            'emergencyContact': emergencyContact.trim(),
        },
      );

      return response.data['data'] as Map<String, dynamic>;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<Map<String, dynamic>?> getMyAdmission() async {
    try {
      final response = await apiClient.dio.get('/admissions/my-admission');
      if (response.data['data'] != null) {
        return response.data['data'] as Map<String, dynamic>;
      }
      return null;
    } catch (e) {
      if (e is ApiException) {
        if (e.statusCode == 404) return null;
        rethrow;
      }
      throw apiClient.handleDioError(e as dynamic);
    }
  }
}
