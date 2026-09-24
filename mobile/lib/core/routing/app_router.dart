import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../features/admin/admin_dashboard_screen.dart';
import '../../features/admission/admission_screen.dart';
import '../../features/attendance/attendance_screen.dart';
import '../../features/attendance/qr_scanner_screen.dart';
import '../../features/authentication/auth_provider.dart';
import '../../features/authentication/login_screen.dart';
import '../../features/authentication/register_screen.dart';
import '../../features/dashboard/student_dashboard_screen.dart';
import '../../features/payments/payment_screen.dart';
import '../../features/seats/seat_layout_screen.dart';
import '../../models/seat_model.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  final authNotifier = ref.watch(authNotifierProvider);

  return GoRouter(
    initialLocation: '/login',
    redirect: (context, state) {
      final isAuth = authNotifier.isAuthenticated;
      final isAdmin = authNotifier.isAdmin;
      final isLoggingIn = state.matchedLocation == '/login' || state.matchedLocation == '/register';

      if (!isAuth && !isLoggingIn) {
        return '/login';
      }

      if (isAuth && isLoggingIn) {
        return isAdmin ? '/admin/dashboard' : '/student/dashboard';
      }

      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/register',
        builder: (context, state) => const RegisterScreen(),
      ),
      GoRoute(
        path: '/student/dashboard',
        builder: (context, state) => const StudentDashboardScreen(),
      ),
      GoRoute(
        path: '/student/seats',
        builder: (context, state) => const SeatLayoutScreen(),
      ),
      GoRoute(
        path: '/student/admission',
        builder: (context, state) {
          final seat = state.extra as SeatModel?;
          if (seat == null) {
            return const SeatLayoutScreen();
          }
          return AdmissionScreen(seat: seat);
        },
      ),
      GoRoute(
        path: '/student/attendance',
        builder: (context, state) => const AttendanceScreen(),
      ),
      GoRoute(
        path: '/student/qr-scanner',
        builder: (context, state) => const QrScannerScreen(),
      ),
      GoRoute(
        path: '/student/payments',
        builder: (context, state) => const PaymentScreen(),
      ),
      GoRoute(
        path: '/admin/dashboard',
        builder: (context, state) => const AdminDashboardScreen(),
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Text('Page not found: ${state.error}'),
      ),
    ),
  );
});
