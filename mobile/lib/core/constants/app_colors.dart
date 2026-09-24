import 'package:flutter/material.dart';

class AppColors {
  // Brand & Accent Colors
  static const Color primary = Color(0xFF0D9488); // Deep Teal / Emerald
  static const Color primaryDark = Color(0xFF0F766E);
  static const Color primaryLight = Color(0xFF14B8A6);
  static const Color accent = Color(0xFFF59E0B); // Amber Accent
  static const Color accentLight = Color(0xFFFBBF24);

  // Backgrounds & Surfaces (Dark Slate Palette)
  static const Color background = Color(0xFF0F172A); // Slate 900
  static const Color surface = Color(0xFF1E293B); // Slate 800
  static const Color surfaceLight = Color(0xFF334155); // Slate 700
  static const Color surfaceElevated = Color(0xFF243247);

  // Borders & Dividers
  static const Color border = Color(0xFF334155);
  static const Color borderSubtle = Color(0xFF1E293B);
  static const Color divider = Color(0xFF1E293B);

  // Typography
  static const Color textPrimary = Color(0xFFF8FAFC); // Slate 50
  static const Color textSecondary = Color(0xFF94A3B8); // Slate 400
  static const Color textMuted = Color(0xFF64748B); // Slate 500
  static const Color textInverse = Color(0xFF0F172A);

  // Seat Status Palette (Crisp & Semantic)
  static const Color seatAvailable = Color(0xFF10B981); // Emerald Green
  static const Color seatOccupied = Color(0xFFEF4444); // Coral Red
  static const Color seatReserved = Color(0xFFF59E0B); // Amber Gold
  static const Color seatMaintenance = Color(0xFF64748B); // Slate Grey
  static const Color seatSelected = Color(0xFF38BDF8); // Electric Sky Blue

  // Status & Feedback Colors
  static const Color success = Color(0xFF10B981);
  static const Color warning = Color(0xFFF59E0B);
  static const Color error = Color(0xFFEF4444);
  static const Color info = Color(0xFF3B82F6);

  // Gradients
  static const LinearGradient primaryGradient = LinearGradient(
    colors: [Color(0xFF0D9488), Color(0xFF0284C7)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient cardGradient = LinearGradient(
    colors: [Color(0xFF1E293B), Color(0xFF0F172A)],
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
  );

  static const LinearGradient glowGradient = LinearGradient(
    colors: [Color(0x330D9488), Color(0x000F172A)],
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
  );
}
