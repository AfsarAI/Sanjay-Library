import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_colors.dart';
import '../../core/providers/core_providers.dart';
import '../../models/library_settings_model.dart';
import 'admin_provider.dart';

class AdminSettingsScreen extends ConsumerStatefulWidget {
  const AdminSettingsScreen({super.key});

  @override
  ConsumerState<AdminSettingsScreen> createState() => _AdminSettingsScreenState();
}

class _AdminSettingsScreenState extends ConsumerState<AdminSettingsScreen> {
  final _formKey = GlobalKey<FormState>();

  late TextEditingController _feeController;
  late TextEditingController _gracePeriodController;
  late TextEditingController _blockDaysController;
  late TextEditingController _releaseDaysController;
  late TextEditingController _timeoutController;
  bool _allowQr = true;
  bool _isSaving = false;
  bool _isInitialized = false;

  @override
  void initState() {
    super.initState();
    _feeController = TextEditingController();
    _gracePeriodController = TextEditingController();
    _blockDaysController = TextEditingController();
    _releaseDaysController = TextEditingController();
    _timeoutController = TextEditingController();
  }

  void _populateForm(LibrarySettingsModel settings) {
    if (_isInitialized) return;
    _feeController.text = settings.monthlyFeeAmount.toStringAsFixed(0);
    _gracePeriodController.text = settings.gracePeriodDays.toString();
    _blockDaysController.text = settings.attendanceBlockAfterDays.toString();
    _releaseDaysController.text = settings.seatReleaseAfterDays.toString();
    _timeoutController.text = settings.reservationTimeoutMinutes.toString();
    _allowQr = settings.allowQrAttendance;
    _isInitialized = true;
  }

  @override
  void dispose() {
    _feeController.dispose();
    _gracePeriodController.dispose();
    _blockDaysController.dispose();
    _releaseDaysController.dispose();
    _timeoutController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final settingsAsync = ref.watch(librarySettingsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Library Policy & Billing Settings'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            onPressed: () {
              setState(() => _isInitialized = false);
              ref.invalidate(librarySettingsProvider);
            },
          ),
        ],
      ),
      body: settingsAsync.when(
        loading: () => const Center(
          child: CircularProgressIndicator(color: AppColors.primary),
        ),
        error: (err, _) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.error_outline_rounded, color: AppColors.error, size: 40),
                const SizedBox(height: 12),
                Text('Error loading settings: $err', textAlign: TextAlign.center),
                const SizedBox(height: 12),
                ElevatedButton(
                  onPressed: () => ref.invalidate(librarySettingsProvider),
                  child: const Text('Retry'),
                ),
              ],
            ),
          ),
        ),
        data: (settings) {
          _populateForm(settings);

          return SingleChildScrollView(
            padding: const EdgeInsets.all(20),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Info banner
                  Container(
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(
                      color: AppColors.primary.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: AppColors.primary.withOpacity(0.3)),
                    ),
                    child: const Row(
                      children: [
                        Icon(Icons.tune_rounded, color: AppColors.primary, size: 22),
                        SizedBox(width: 12),
                        Expanded(
                          child: Text(
                            'Update policies dynamically. Changes apply immediately to new cycles and attendance enforcement without restarting.',
                            style: TextStyle(fontSize: 12, color: AppColors.textSecondary, height: 1.4),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),

                  const Text(
                    'Pricing & Fees',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: _feeController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Standard Monthly Fee (₹)',
                      hintText: 'e.g. 700',
                      prefixIcon: Icon(Icons.currency_rupee_rounded),
                    ),
                    validator: (val) {
                      if (val == null || val.isEmpty) return 'Enter monthly fee';
                      if (double.tryParse(val) == null || double.parse(val) <= 0) {
                        return 'Enter a valid positive fee';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 24),

                  const Text(
                    'Billing Timeline & Delinquency Rules',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: _gracePeriodController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Grace Period (Days)',
                      helperText: 'Number of days student can study after due date before blocking',
                      prefixIcon: Icon(Icons.hourglass_top_rounded),
                    ),
                    validator: (val) {
                      if (val == null || int.tryParse(val) == null) return 'Enter valid days';
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _blockDaysController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Attendance Block After (Days)',
                      helperText: 'Days past due date when QR scanner blocks entrance check-in',
                      prefixIcon: Icon(Icons.block_rounded),
                    ),
                    validator: (val) {
                      if (val == null || int.tryParse(val) == null) return 'Enter valid days';
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _releaseDaysController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Auto Seat Release After (Days)',
                      helperText: 'Days after due date when unoccupied seat is released to public',
                      prefixIcon: Icon(Icons.event_seat_rounded),
                    ),
                    validator: (val) {
                      if (val == null || int.tryParse(val) == null) return 'Enter valid days';
                      return null;
                    },
                  ),
                  const SizedBox(height: 24),

                  const Text(
                    'Seat Booking & Verification',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: _timeoutController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Temporary Reservation Lock Timeout (Minutes)',
                      helperText: 'Time a student has to complete admission payment before seat unlocks',
                      prefixIcon: Icon(Icons.timer_outlined),
                    ),
                    validator: (val) {
                      if (val == null || int.tryParse(val) == null) return 'Enter valid minutes';
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),
                  SwitchListTile(
                    title: const Text('Allow QR Attendance Verification'),
                    subtitle: const Text('Enable rotating QR code scanning at front desk'),
                    value: _allowQr,
                    activeColor: AppColors.primary,
                    contentPadding: EdgeInsets.zero,
                    onChanged: (val) => setState(() => _allowQr = val),
                  ),
                  const SizedBox(height: 32),

                  SizedBox(
                    width: double.infinity,
                    height: 50,
                    child: ElevatedButton.icon(
                      icon: _isSaving
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                            )
                          : const Icon(Icons.save_rounded),
                      label: Text(_isSaving ? 'Saving...' : 'Save Policy Changes'),
                      onPressed: _isSaving ? null : () => _saveSettings(settings),
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  void _saveSettings(LibrarySettingsModel existing) async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSaving = true);
    try {
      final updatedModel = LibrarySettingsModel(
        id: existing.id,
        libraryId: existing.libraryId,
        monthlyFeeAmount: double.parse(_feeController.text),
        gracePeriodDays: int.parse(_gracePeriodController.text),
        attendanceBlockAfterDays: int.parse(_blockDaysController.text),
        seatReleaseAfterDays: int.parse(_releaseDaysController.text),
        reservationTimeoutMinutes: int.parse(_timeoutController.text),
        allowQrAttendance: _allowQr,
      );

      final adminService = ref.read(adminServiceProvider);
      await adminService.updateSettings(updatedModel);

      if (mounted) {
        ref.invalidate(librarySettingsProvider);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            backgroundColor: AppColors.success,
            content: Text('Library policy and fee settings updated successfully!'),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(backgroundColor: AppColors.error, content: Text('Error saving: $e')),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSaving = false);
      }
    }
  }
}
