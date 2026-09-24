import '../core/network/api_client.dart';
import '../core/security/token_storage.dart';
import '../models/user_model.dart';

class AuthService {
  final ApiClient apiClient;
  final TokenStorage tokenStorage;

  AuthService({required this.apiClient, required this.tokenStorage});

  Future<UserModel> login({
    required String phoneNumber,
    required String password,
  }) async {
    try {
      final response = await apiClient.dio.post(
        '/auth/login',
        data: {
          'phoneNumber': phoneNumber.trim(),
          'password': password,
        },
      );

      final data = response.data['data'] as Map<String, dynamic>;
      final accessToken = data['accessToken'] as String;
      final refreshToken = data['refreshToken'] as String;
      final userJson = data['user'] as Map<String, dynamic>;
      final user = UserModel.fromJson(userJson);

      await tokenStorage.saveAuthSession(
        accessToken: accessToken,
        refreshToken: refreshToken,
        userId: user.id,
        role: user.role,
        name: user.fullName,
        phone: user.phoneNumber,
      );

      return user;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<UserModel> register({
    required String phoneNumber,
    required String fullName,
    required String password,
    String? email,
  }) async {
    try {
      final response = await apiClient.dio.post(
        '/auth/register',
        data: {
          'phoneNumber': phoneNumber.trim(),
          'fullName': fullName.trim(),
          'password': password,
          if (email != null && email.isNotEmpty) 'email': email.trim(),
        },
      );

      final data = response.data['data'] as Map<String, dynamic>;
      final accessToken = data['accessToken'] as String;
      final refreshToken = data['refreshToken'] as String;
      final userJson = data['user'] as Map<String, dynamic>;
      final user = UserModel.fromJson(userJson);

      await tokenStorage.saveAuthSession(
        accessToken: accessToken,
        refreshToken: refreshToken,
        userId: user.id,
        role: user.role,
        name: user.fullName,
        phone: user.phoneNumber,
      );

      return user;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw apiClient.handleDioError(e as dynamic);
    }
  }

  Future<void> logout() async {
    try {
      final refreshToken = await tokenStorage.getRefreshToken();
      if (refreshToken != null) {
        await apiClient.dio.post(
          '/auth/logout',
          data: {'refreshToken': refreshToken},
        );
      }
    } catch (_) {
      // Ignore network failure on logout
    } finally {
      await tokenStorage.clearAll();
    }
  }
}
