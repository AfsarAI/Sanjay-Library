import '../core/network/api_client.dart';
import '../models/payment_model.dart';

class PaymentService {
  final ApiClient apiClient;

  PaymentService({required this.apiClient});

  Future<Map<String, dynamic>> createRazorpayOrder({
    required double amount,
    int? subscriptionId,
  }) async {
    try {
      final response = await apiClient.dio.post(
        '/payments/create-order',
        data: {
          'amount': amount,
          if (subscriptionId != null) 'subscriptionId': subscriptionId,
        },
      );
      return response.data['data'] as Map<String, dynamic>;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<PaymentModel> verifyRazorpayPayment({
    required String orderId,
    required String paymentId,
    required String signature,
    int? subscriptionId,
  }) async {
    try {
      final response = await apiClient.dio.post(
        '/payments/verify',
        data: {
          'razorpayOrderId': orderId,
          'razorpayPaymentId': paymentId,
          'razorpaySignature': signature,
          if (subscriptionId != null) 'subscriptionId': subscriptionId,
        },
      );
      return PaymentModel.fromJson(response.data['data'] as Map<String, dynamic>);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<PaymentModel> recordCashPayment({
    required int studentId,
    required double amount,
    int? subscriptionId,
    String? notes,
  }) async {
    try {
      final response = await apiClient.dio.post(
        '/payments/cash',
        data: {
          'studentId': studentId,
          'amount': amount,
          if (subscriptionId != null) 'subscriptionId': subscriptionId,
          if (notes != null && notes.isNotEmpty) 'notes': notes,
        },
      );
      return PaymentModel.fromJson(response.data['data'] as Map<String, dynamic>);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<List<PaymentModel>> getMyPaymentHistory({int page = 0, int size = 20}) async {
    try {
      final response = await apiClient.dio.get(
        '/payments/my-history',
        queryParameters: {'page': page, 'size': size},
      );
      final pageData = response.data['data'] as Map<String, dynamic>;
      final content = pageData['content'] as List<dynamic>? ?? [];
      return content.map((e) => PaymentModel.fromJson(e as Map<String, dynamic>)).toList();
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }
}
