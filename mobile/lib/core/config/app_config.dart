import 'package:flutter/foundation.dart';

class AppConfig {
  static const String appName = 'Sanjay Library';
  static const String appDescription = 'Digital Library Management System for Sanjay Library';
  static const String appVersion = '1.0.0';

  // Detected local Wi-Fi LAN IP of laptop running Spring Boot
  static const String defaultLanIp = '192.168.31.151';
  static const String defaultLanApiBaseUrl = 'http://$defaultLanIp:8080/api/v1';

  static const String _envApiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: '',
  );

  /// Resolves the API base URL intelligently:
  /// 1. Uses `--dart-define=API_BASE_URL=...` if provided.
  /// 2. If running on Web: dynamically uses `Uri.base.host` to reach backend on port 8080.
  /// 3. If running on Native Android/iOS: defaults to the laptop's LAN IP (`http://192.168.31.151:8080/api/v1`).
  static String get apiBaseUrl {
    if (_envApiBaseUrl.isNotEmpty) {
      return _envApiBaseUrl;
    }
    if (kIsWeb) {
      final host = Uri.base.host;
      if (host.isNotEmpty && host != 'localhost' && host != '127.0.0.1') {
        final scheme = Uri.base.scheme.isNotEmpty ? Uri.base.scheme : 'http';
        return '$scheme://$host:8080/api/v1';
      }
      return 'http://localhost:8080/api/v1';
    }
    return defaultLanApiBaseUrl;
  }

  static const int connectTimeoutMs = 15000;
  static const int receiveTimeoutMs = 15000;

  static const int defaultLibraryId = 1;

  // Razorpay public key for client-side checkout
  static const String razorpayKeyId = String.fromEnvironment(
    'RAZORPAY_KEY_ID',
    defaultValue: 'rzp_test_placeholder_key',
  );

  static const String libraryHelplinePhone = '+919876543210';
}
