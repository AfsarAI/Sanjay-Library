import 'dart:io';
import 'package:dio/dio.dart';
import '../config/app_config.dart';
import '../security/token_storage.dart';

class ApiException implements Exception {
  final String message;
  final String? code;
  final int? statusCode;
  final Map<String, dynamic>? details;

  ApiException({
    required this.message,
    this.code,
    this.statusCode,
    this.details,
  });

  @override
  String toString() => message;
}

class ApiClient {
  late final Dio dio;
  final TokenStorage tokenStorage;

  ApiClient({required this.tokenStorage}) {
    dio = Dio(
      BaseOptions(
        baseUrl: AppConfig.apiBaseUrl,
        connectTimeout: const Duration(milliseconds: AppConfig.connectTimeoutMs),
        receiveTimeout: const Duration(milliseconds: AppConfig.receiveTimeoutMs),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      ),
    );

    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          final token = await tokenStorage.getAccessToken();
          if (token != null && token.isNotEmpty) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          return handler.next(options);
        },
        onError: (DioException error, handler) async {
          // Handle 401 Unauthorized by attempting token refresh
          if (error.response?.statusCode == 401 &&
              !error.requestOptions.path.contains('/auth/login') &&
              !error.requestOptions.path.contains('/auth/refresh-token')) {
            final refreshToken = await tokenStorage.getRefreshToken();
            if (refreshToken != null && refreshToken.isNotEmpty) {
              try {
                final refreshDio = Dio(
                  BaseOptions(
                    baseUrl: AppConfig.apiBaseUrl,
                    headers: {'Content-Type': 'application/json'},
                  ),
                );

                final response = await refreshDio.post(
                  '/auth/refresh-token',
                  data: {'refreshToken': refreshToken},
                );

                if (response.statusCode == 200 && response.data['success'] == true) {
                  final newAccessToken = response.data['data']['accessToken'] as String;
                  await tokenStorage.updateAccessToken(newAccessToken);

                  // Retry the original request
                  final retryOptions = error.requestOptions;
                  retryOptions.headers['Authorization'] = 'Bearer $newAccessToken';
                  final cloneReq = await dio.fetch(retryOptions);
                  return handler.resolve(cloneReq);
                }
              } catch (_) {
                await tokenStorage.clearAll();
              }
            }
          }
          return handler.next(error);
        },
      ),
    );
  }

  ApiException handleDioError(DioException error) {
    if (error.type == DioExceptionType.connectionTimeout ||
        error.type == DioExceptionType.receiveTimeout ||
        error.type == DioExceptionType.sendTimeout) {
      return ApiException(
        message: 'Connection timed out. Please check your internet connection.',
        code: 'TIMEOUT',
      );
    }

    if (error.error is SocketException) {
      return ApiException(
        message: 'Unable to reach the server. Please check your network connection.',
        code: 'NETWORK_ERROR',
      );
    }

    if (error.response != null && error.response?.data is Map) {
      final data = error.response!.data as Map<String, dynamic>;
      if (data.containsKey('error') && data['error'] is Map) {
        final errObj = data['error'] as Map<String, dynamic>;
        String message = errObj['message']?.toString() ?? 'An error occurred';
        if (errObj['details'] is Map && (errObj['details'] as Map).isNotEmpty) {
          final details = errObj['details'] as Map<String, dynamic>;
          final firstField = details.values.first.toString();
          message = '$message: $firstField';
        }
        return ApiException(
          message: message,
          code: errObj['code']?.toString(),
          statusCode: error.response?.statusCode,
          details: errObj['details'] is Map ? (errObj['details'] as Map<String, dynamic>) : null,
        );
      }
    }

    return ApiException(
      message: error.response?.statusMessage ?? 'Something went wrong. Please try again.',
      statusCode: error.response?.statusCode,
    );
  }
}
