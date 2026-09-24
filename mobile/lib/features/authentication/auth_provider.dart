import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/network/api_client.dart';
import '../../core/providers/core_providers.dart';
import '../../models/user_model.dart';
import '../../services/auth_service.dart';
import 'auth_state.dart';

final authNotifierProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  final authService = ref.watch(authServiceProvider);
  return AuthNotifier(authService: authService, ref: ref);
});

class AuthNotifier extends StateNotifier<AuthState> {
  final AuthService authService;
  final Ref ref;

  AuthNotifier({required this.authService, required this.ref}) : super(const AuthState()) {
    checkInitialAuth();
  }

  Future<void> checkInitialAuth() async {
    final tokenStorage = ref.read(tokenStorageProvider);
    final hasToken = await tokenStorage.hasValidToken();
    if (!hasToken) {
      state = state.copyWith(status: AuthStatus.unauthenticated);
      return;
    }

    final userId = await tokenStorage.getUserId();
    final role = await tokenStorage.getUserRole();
    final name = await tokenStorage.getUserName();
    final phone = await tokenStorage.getUserPhone();

    if (userId != null && role != null) {
      final user = UserModel(
        id: userId,
        libraryId: 1,
        phoneNumber: phone ?? '',
        fullName: name ?? 'User',
        role: role,
        status: 'ACTIVE',
      );
      state = state.copyWith(status: AuthStatus.authenticated, user: user);
    } else {
      state = state.copyWith(status: AuthStatus.unauthenticated);
    }
  }

  Future<bool> login({
    required String phoneNumber,
    required String password,
  }) async {
    state = state.copyWith(status: AuthStatus.loading, errorMessage: null);
    try {
      final user = await authService.login(
        phoneNumber: phoneNumber,
        password: password,
      );
      state = state.copyWith(
        status: AuthStatus.authenticated,
        user: user,
      );
      return true;
    } catch (e) {
      String msg = 'Login failed. Please check credentials.';
      if (e is ApiException) msg = e.message;
      state = state.copyWith(
        status: AuthStatus.error,
        errorMessage: msg,
      );
      return false;
    }
  }

  Future<bool> register({
    required String phoneNumber,
    required String fullName,
    required String password,
    String? email,
  }) async {
    state = state.copyWith(status: AuthStatus.loading, errorMessage: null);
    try {
      final user = await authService.register(
        phoneNumber: phoneNumber,
        fullName: fullName,
        password: password,
        email: email,
      );
      state = state.copyWith(
        status: AuthStatus.authenticated,
        user: user,
      );
      return true;
    } catch (e) {
      String msg = 'Registration failed. Please try again.';
      if (e is ApiException) msg = e.message;
      state = state.copyWith(
        status: AuthStatus.error,
        errorMessage: msg,
      );
      return false;
    }
  }

  Future<void> logout() async {
    state = state.copyWith(status: AuthStatus.loading);
    await authService.logout();
    state = const AuthState(status: AuthStatus.unauthenticated);
  }
}
