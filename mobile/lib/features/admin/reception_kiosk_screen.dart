import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:qr_flutter/qr_flutter.dart';
import '../../core/constants/app_colors.dart';
import 'admin_provider.dart';

class ReceptionKioskScreen extends ConsumerStatefulWidget {
  const ReceptionKioskScreen({super.key});

  @override
  ConsumerState<ReceptionKioskScreen> createState() => _ReceptionKioskScreenState();
}

class _ReceptionKioskScreenState extends ConsumerState<ReceptionKioskScreen> {
  static const int _rotationSeconds = 30;
  int _secondsRemaining = _rotationSeconds;
  Timer? _countdownTimer;

  @override
  void initState() {
    super.initState();
    _startTimer();
  }

  void _startTimer() {
    _countdownTimer?.cancel();
    _secondsRemaining = _rotationSeconds;
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (mounted) {
        setState(() {
          if (_secondsRemaining > 1) {
            _secondsRemaining--;
          } else {
            _secondsRemaining = _rotationSeconds;
            ref.invalidate(receptionQrTokenProvider);
          }
        });
      }
    });
  }

  @override
  void dispose() {
    _countdownTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final qrTokenAsync = ref.watch(receptionQrTokenProvider);

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Reception Kiosk Mode'),
        backgroundColor: AppColors.surface,
        actions: [
          IconButton(
            tooltip: 'Force Refresh QR',
            icon: const Icon(Icons.refresh_rounded),
            onPressed: () {
              ref.invalidate(receptionQrTokenProvider);
              _startTimer();
            },
          ),
          IconButton(
            tooltip: 'Close Kiosk',
            icon: const Icon(Icons.close_rounded),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Header Card
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  color: AppColors.primary.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(24),
                  border: Border.all(color: AppColors.primary.withOpacity(0.4)),
                ),
                child: const Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.verified_user_rounded, color: AppColors.primary, size: 18),
                    SizedBox(width: 8),
                    Text(
                      'Anti-Proxy Rotating Cryptographic QR Active',
                      style: TextStyle(
                        color: AppColors.primary,
                        fontWeight: FontWeight.w600,
                        fontSize: 13,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              const Text(
                'Sanjay Library Reception Desk',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  letterSpacing: -0.5,
                ),
              ),
              const SizedBox(height: 6),
              const Text(
                'Open Sanjay Library mobile app & scan to Check-In or Check-Out',
                textAlign: TextAlign.center,
                style: TextStyle(color: AppColors.textSecondary, fontSize: 14),
              ),
              const SizedBox(height: 28),

              // QR Code Display Container
              Container(
                width: 320,
                height: 320,
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.primary.withOpacity(0.2),
                      blurRadius: 30,
                      offset: const Offset(0, 10),
                    ),
                  ],
                ),
                child: qrTokenAsync.when(
                  loading: () => const Center(
                    child: CircularProgressIndicator(color: AppColors.primary),
                  ),
                  error: (err, _) => Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(Icons.error_outline_rounded, color: AppColors.error, size: 40),
                        const SizedBox(height: 8),
                        Text(
                          'Error loading QR: $err',
                          textAlign: TextAlign.center,
                          style: const TextStyle(color: Colors.black87, fontSize: 12),
                        ),
                        const SizedBox(height: 12),
                        ElevatedButton(
                          onPressed: () => ref.invalidate(receptionQrTokenProvider),
                          child: const Text('Retry'),
                        ),
                      ],
                    ),
                  ),
                  data: (token) => QrImageView(
                    data: token,
                    version: QrVersions.auto,
                    size: 270.0,
                    backgroundColor: Colors.white,
                    eyeStyle: const QrEyeStyle(
                      eyeShape: QrEyeShape.square,
                      color: Colors.black,
                    ),
                    dataModuleStyle: const QrDataModuleStyle(
                      dataModuleShape: QrDataModuleShape.square,
                      color: Colors.black,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 24),

              // Rotating Timer Progress & Countdown
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  SizedBox(
                    width: 28,
                    height: 28,
                    child: CircularProgressIndicator(
                      value: _secondsRemaining / _rotationSeconds,
                      strokeWidth: 3,
                      backgroundColor: AppColors.surfaceLight,
                      color: _secondsRemaining <= 5 ? AppColors.error : AppColors.primary,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Text(
                    'Rotates in $_secondsRemaining seconds',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: _secondsRemaining <= 5 ? AppColors.error : AppColors.textSecondary,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 28),

              // Instructions Footnote
              Container(
                constraints: const BoxConstraints(maxWidth: 420),
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: AppColors.border),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.info_outline_rounded, color: AppColors.accent, size: 24),
                    SizedBox(width: 14),
                    Expanded(
                      child: Text(
                        'Keep this screen open on the tablet at the entrance. Each token expires automatically after 30 seconds to prevent shared screenshots.',
                        style: TextStyle(fontSize: 12, color: AppColors.textSecondary, height: 1.4),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
