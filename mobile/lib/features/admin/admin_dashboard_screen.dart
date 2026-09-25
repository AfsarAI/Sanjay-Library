import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/constants/app_colors.dart';
import '../../core/providers/core_providers.dart';
import '../../core/utils/formatters.dart';
import '../authentication/auth_provider.dart';
import '../seats/seat_provider.dart';
import 'admin_provider.dart';

class AdminDashboardScreen extends ConsumerWidget {
  const AdminDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final dashboardAsync = ref.watch(adminDashboardProvider);
    final authState = ref.watch(authNotifierProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Owner Admin Portal', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            Text('Digital Library Central Control', style: TextStyle(fontSize: 11, color: AppColors.textSecondary)),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            onPressed: () {
              ref.invalidate(adminDashboardProvider);
              ref.invalidate(seatLayoutProvider);
            },
          ),
          IconButton(
            icon: const Icon(Icons.logout_rounded),
            onPressed: () => ref.read(authNotifierProvider.notifier).logout(),
          ),
        ],
      ),
      body: RefreshIndicator(
        color: AppColors.primary,
        onRefresh: () async {
          ref.invalidate(adminDashboardProvider);
          ref.invalidate(seatLayoutProvider);
        },
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Admin Greeting Header
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Welcome, ${authState.user?.fullName ?? "Owner"}',
                    style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.accent.withOpacity(0.15),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Text(
                      'ADMIN',
                      style: TextStyle(color: AppColors.accent, fontWeight: FontWeight.bold, fontSize: 11),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 16),

              // Real-time Metrics Grid
              dashboardAsync.when(
                loading: () => const Center(
                  child: Padding(
                    padding: EdgeInsets.all(40),
                    child: CircularProgressIndicator(color: AppColors.primary),
                  ),
                ),
                error: (err, _) => Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: AppColors.error.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text('Failed to load metrics: $err', style: const TextStyle(color: AppColors.error)),
                ),
                data: (metrics) {
                  return Column(
                    children: [
                      // Occupancy & Inside Cards
                      Row(
                        children: [
                          Expanded(
                            child: _buildMetricCard(
                              title: 'Occupancy',
                              value: '${metrics.occupiedSeats} / ${metrics.totalSeats}',
                              subtitle: '${((metrics.occupiedSeats / (metrics.totalSeats > 0 ? metrics.totalSeats : 1)) * 100).toStringAsFixed(0)}% Occupied',
                              icon: Icons.event_seat_rounded,
                              color: AppColors.seatAvailable,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: _buildMetricCard(
                              title: 'Inside Now',
                              value: '${metrics.currentlyInsideCount}',
                              subtitle: '${metrics.todayAttendanceCount} arrived today',
                              icon: Icons.people_alt_rounded,
                              color: AppColors.info,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          Expanded(
                            child: _buildMetricCard(
                              title: 'Fees Due / Overdue',
                              value: '${metrics.feesDueCount} Due',
                              subtitle: '${metrics.overdueCount} Overdue students',
                              icon: Icons.warning_amber_rounded,
                              color: AppColors.warning,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: _buildMetricCard(
                              title: 'Revenue Collected',
                              value: Formatters.formatCurrency(metrics.monthlyRevenue),
                              subtitle: 'Pending: ${Formatters.formatCurrency(metrics.pendingRevenue)}',
                              icon: Icons.currency_rupee_rounded,
                              color: AppColors.success,
                            ),
                          ),
                        ],
                      ),
                    ],
                  );
                },
              ),

              const SizedBox(height: 24),
              const Text(
                'Facility & Student Operations',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 12),

              // Action Buttons
              _buildActionTile(
                icon: Icons.grid_view_rounded,
                title: 'Inspect 50 Desks & Release Seats',
                subtitle: 'View live 2D grid, change desk states, or release vacant desks',
                color: AppColors.primaryLight,
                onTap: () => context.push('/student/seats'),
              ),
              const SizedBox(height: 10),
              _buildActionTile(
                icon: Icons.payments_rounded,
                title: 'Record Offline Cash Payment',
                subtitle: 'Log direct cash payment from student and extend monthly cycle',
                color: AppColors.accent,
                onTap: () => _showRecordCashDialog(context, ref),
              ),
              const SizedBox(height: 10),
              _buildActionTile(
                icon: Icons.more_time_rounded,
                title: 'Grant Student Grace Extension',
                subtitle: 'Provide grace period extension to re-enable blocked attendance',
                color: AppColors.info,
                onTap: () => _showGrantExtensionDialog(context, ref),
              ),

              const SizedBox(height: 24),
              const Text(
                'Live Desk Quick Status',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),

              // Preview button to visual layout
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: AppColors.border),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.meeting_room_rounded, color: AppColors.primaryLight, size: 28),
                    const SizedBox(width: 14),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Self-Study Hall (50 Desks)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                          Text('Real-time 2D color-coded layout (A01 - A50)', style: TextStyle(color: AppColors.textSecondary, fontSize: 12)),
                        ],
                      ),
                    ),
                    ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                      ),
                      onPressed: () => context.push('/student/seats'),
                      child: const Text('Open Map'),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 24),
              const Text(
                'Owner Command Center',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 12),

              // Reception Kiosk Mode Tile
              Card(
                color: AppColors.surface,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                  side: const BorderSide(color: AppColors.border),
                ),
                child: ListTile(
                  contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  leading: CircleAvatar(
                    backgroundColor: AppColors.accent.withOpacity(0.15),
                    child: const Icon(Icons.qr_code_2_rounded, color: AppColors.accent),
                  ),
                  title: const Text('Reception Kiosk Mode', style: TextStyle(fontWeight: FontWeight.bold)),
                  subtitle: const Text('Display 30s rotating QR code on tablet for entrance check-in', style: TextStyle(fontSize: 12, color: AppColors.textSecondary)),
                  trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 16, color: AppColors.textSecondary),
                  onTap: () => context.push('/admin/kiosk'),
                ),
              ),
              const SizedBox(height: 10),

              // Student Directory Tile
              Card(
                color: AppColors.surface,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                  side: const BorderSide(color: AppColors.border),
                ),
                child: ListTile(
                  contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  leading: CircleAvatar(
                    backgroundColor: AppColors.primary.withOpacity(0.15),
                    child: const Icon(Icons.people_alt_rounded, color: AppColors.primary),
                  ),
                  title: const Text('Student Directory & Status', style: TextStyle(fontWeight: FontWeight.bold)),
                  subtitle: const Text('Search admissions, assigned seats, record cash payments', style: TextStyle(fontSize: 12, color: AppColors.textSecondary)),
                  trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 16, color: AppColors.textSecondary),
                  onTap: () => context.push('/admin/students'),
                ),
              ),
              const SizedBox(height: 10),

              // Policy & Billing Settings Tile
              Card(
                color: AppColors.surface,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                  side: const BorderSide(color: AppColors.border),
                ),
                child: ListTile(
                  contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  leading: CircleAvatar(
                    backgroundColor: AppColors.warning.withOpacity(0.15),
                    child: const Icon(Icons.settings_suggest_rounded, color: AppColors.warning),
                  ),
                  title: const Text('Policy & Billing Rules', style: TextStyle(fontWeight: FontWeight.bold)),
                  subtitle: const Text('Adjust monthly fee, grace period, seat release days', style: TextStyle(fontSize: 12, color: AppColors.textSecondary)),
                  trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 16, color: AppColors.textSecondary),
                  onTap: () => context.push('/admin/settings'),
                ),
              ),
              const SizedBox(height: 24),

              // Reports & Export
              const Text(
                'Reports & Export',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 12),
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: AppColors.border),
                ),
                child: Column(
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.file_download_outlined, color: AppColors.success, size: 24),
                        const SizedBox(width: 12),
                        const Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Attendance Register (CSV)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                              Text('Export complete 30-day student entry/exit records', style: TextStyle(color: AppColors.textSecondary, fontSize: 12)),
                            ],
                          ),
                        ),
                        OutlinedButton(
                          onPressed: () {
                            final url = ref.read(adminServiceProvider).getAttendanceReportCsvUrl();
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                content: Text('Attendance CSV report available at:\n$url'),
                                action: SnackBarAction(
                                  label: 'OK',
                                  onPressed: () {},
                                ),
                              ),
                            );
                          },
                          child: const Text('Export'),
                        ),
                      ],
                    ),
                    const Divider(height: 24),
                    Row(
                      children: [
                        const Icon(Icons.receipt_long_rounded, color: AppColors.accent, size: 24),
                        const SizedBox(width: 12),
                        const Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Monthly Revenue Ledger (CSV)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                              Text('Export all online & cash fee collections', style: TextStyle(color: AppColors.textSecondary, fontSize: 12)),
                            ],
                          ),
                        ),
                        OutlinedButton(
                          onPressed: () {
                            final url = ref.read(adminServiceProvider).getRevenueReportCsvUrl();
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                content: Text('Revenue CSV report available at:\n$url'),
                                action: SnackBarAction(
                                  label: 'OK',
                                  onPressed: () {},
                                ),
                              ),
                            );
                          },
                          child: const Text('Export'),
                        ),
                      ],
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

  Widget _buildMetricCard({
    required String title,
    required String value,
    required String subtitle,
    required IconData icon,
    required Color color,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                title,
                style: const TextStyle(color: AppColors.textSecondary, fontSize: 12, fontWeight: FontWeight.w600),
              ),
              Icon(icon, color: color, size: 18),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            value,
            style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 4),
          Text(
            subtitle,
            style: TextStyle(color: color, fontSize: 11, fontWeight: FontWeight.w500),
          ),
        ],
      ),
    );
  }

  Widget _buildActionTile({
    required IconData icon,
    required String title,
    required String subtitle,
    required Color color,
    required VoidCallback onTap,
  }) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: AppColors.border),
          ),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: color.withOpacity(0.15),
                  shape: BoxShape.circle,
                ),
                child: Icon(icon, color: color, size: 24),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                    const SizedBox(height: 2),
                    Text(subtitle, style: const TextStyle(color: AppColors.textSecondary, fontSize: 12)),
                  ],
                ),
              ),
              const Icon(Icons.arrow_forward_ios_rounded, size: 14, color: AppColors.textMuted),
            ],
          ),
        ),
      ),
    );
  }

  void _showRecordCashDialog(BuildContext context, WidgetRef ref) {
    final studentIdController = TextEditingController(text: '2');
    final amountController = TextEditingController(text: '700');
    final notesController = TextEditingController(text: 'Paid cash at counter');

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Record Cash Payment'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: studentIdController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Student User ID',
                hintText: 'e.g. 2',
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: amountController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Amount (₹)',
                hintText: 'e.g. 700',
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: notesController,
              decoration: const InputDecoration(
                labelText: 'Note / Reference',
                hintText: 'e.g. August fee paid in cash',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () async {
              final studentId = int.tryParse(studentIdController.text);
              final amount = double.tryParse(amountController.text);

              if (studentId != null && amount != null) {
                Navigator.pop(ctx);
                final success = await ref.read(adminActionProvider.notifier).recordCashPayment(
                      studentId: studentId,
                      amount: amount,
                      notes: notesController.text,
                    );

                if (context.mounted && success) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      backgroundColor: AppColors.success,
                      content: Text('Cash payment logged and subscription extended!'),
                    ),
                  );
                }
              }
            },
            child: const Text('Confirm Cash Receipt'),
          ),
        ],
      ),
    );
  }

  void _showGrantExtensionDialog(BuildContext context, WidgetRef ref) {
    final subscriptionIdController = TextEditingController(text: '1');
    DateTime extensionDate = DateTime.now().add(const Duration(days: 7));

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Grant Grace Extension'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: subscriptionIdController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Subscription ID',
                hintText: 'e.g. 1',
              ),
            ),
            const SizedBox(height: 14),
            const Text(
              'Extends grace period by 7 days to enable attendance.',
              style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () async {
              final subId = int.tryParse(subscriptionIdController.text);
              if (subId != null) {
                Navigator.pop(ctx);
                final success = await ref.read(adminActionProvider.notifier).grantExtension(
                      subscriptionId: subId,
                      newGraceDate: extensionDate,
                      reason: 'Owner granted 7-day grace extension',
                    );

                if (context.mounted && success) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      backgroundColor: AppColors.success,
                      content: Text('Extension granted successfully!'),
                    ),
                  );
                }
              }
            },
            child: const Text('Grant 7 Days'),
          ),
        ],
      ),
    );
  }
}
