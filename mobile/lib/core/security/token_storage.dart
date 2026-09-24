import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class TokenStorage {
  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
    iOptions: IOSOptions(accessibility: KeychainAccessibility.first_unlock),
  );

  static const String _keyAccessToken = 'auth_access_token';
  static const String _keyRefreshToken = 'auth_refresh_token';
  static const String _keyUserId = 'auth_user_id';
  static const String _keyUserRole = 'auth_user_role';
  static const String _keyUserName = 'auth_user_name';
  static const String _keyUserPhone = 'auth_user_phone';

  Future<void> saveAuthSession({
    required String accessToken,
    required String refreshToken,
    required int userId,
    required String role,
    required String name,
    required String phone,
  }) async {
    await _storage.write(key: _keyAccessToken, value: accessToken);
    await _storage.write(key: _keyRefreshToken, value: refreshToken);
    await _storage.write(key: _keyUserId, value: userId.toString());
    await _storage.write(key: _keyUserRole, value: role);
    await _storage.write(key: _keyUserName, value: name);
    await _storage.write(key: _keyUserPhone, value: phone);
  }

  Future<void> updateAccessToken(String newAccessToken) async {
    await _storage.write(key: _keyAccessToken, value: newAccessToken);
  }

  Future<String?> getAccessToken() async => await _storage.read(key: _keyAccessToken);
  Future<String?> getRefreshToken() async => await _storage.read(key: _keyRefreshToken);
  Future<String?> getUserRole() async => await _storage.read(key: _keyUserRole);
  Future<String?> getUserName() async => await _storage.read(key: _keyUserName);
  Future<String?> getUserPhone() async => await _storage.read(key: _keyUserPhone);

  Future<int?> getUserId() async {
    final str = await _storage.read(key: _keyUserId);
    return str != null ? int.tryParse(str) : null;
  }

  Future<bool> hasValidToken() async {
    final token = await getAccessToken();
    return token != null && token.isNotEmpty;
  }

  Future<void> clearAll() async {
    await _storage.deleteAll();
  }
}
