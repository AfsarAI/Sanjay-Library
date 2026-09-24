class AppConfig {
  static const String appName = 'Sanjay Library';
  static const String appDescription = 'Digital Library Management System for Sanjay Library';
  static const String appVersion = '1.0.0';

  // Base URL for Spring Boot backend
  // For Android Emulator use 10.0.2.2:8080, for physical device use LAN IP, for local web/desktop use localhost:8080
  static const String defaultApiBaseUrl = 'http://localhost:8080/api/v1';

  static String apiBaseUrl = const String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: defaultApiBaseUrl,
  );

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
