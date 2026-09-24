import 'package:intl/intl.dart';
import '../core/network/api_client.dart';
import '../models/subscription_model.dart';

class SubscriptionService {
  final ApiClient apiClient;

  SubscriptionService({required this.apiClient});

  Future<SubscriptionModel?> getMySubscription() async {
    try {
      final response = await apiClient.dio.get('/subscriptions/my-subscription');
      if (response.data['data'] != null) {
        return SubscriptionModel.fromJson(response.data['data'] as Map<String, dynamic>);
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

  Future<SubscriptionModel> getStudentSubscription(int studentId) async {
    try {
      final response = await apiClient.dio.get('/subscriptions/student/$studentId');
      return SubscriptionModel.fromJson(response.data['data'] as Map<String, dynamic>);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<SubscriptionModel> grantExtension({
    required int subscriptionId,
    required DateTime newGraceDate,
    String reason = 'Admin granted extension',
  }) async {
    try {
      final dateStr = DateFormat('yyyy-MM-dd').format(newGraceDate);
      final response = await apiClient.dio.post(
        '/subscriptions/$subscriptionId/extension',
        queryParameters: {
          'newGraceDate': dateStr,
          'reason': reason,
        },
      );
      return SubscriptionModel.fromJson(response.data['data'] as Map<String, dynamic>);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }
}
