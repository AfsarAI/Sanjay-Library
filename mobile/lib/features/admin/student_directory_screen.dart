import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/app_colors.dart';
import '../../core/providers/core_providers.dart';
import '../../core/utils/formatters.dart';
import '../../models/admin_student_model.dart';
import 'admin_provider.dart';

class StudentDirectoryScreen extends ConsumerStatefulWidget {
  const StudentDirectoryScreen({super.key});

  @override
  ConsumerState<StudentDirectoryScreen> createState() => _StudentDirectoryScreenState();
}

class _StudentDirectoryScreenState extends ConsumerState<StudentDirectoryScreen> {
  final TextEditingController _searchController = TextEditingController();
  String _selectedStatus = 'ALL';
  String _searchQuery = '';

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final filter = (query: _searchQuery, status: _selectedStatus);
    final studentsAsync = ref.watch(studentDirectoryProvider(filter));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Student Directory'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            onPressed: () => ref.invalidate(studentDirectoryProvider(filter)),
          ),
        ],
      ),
      body: Column(
        children: [
          // Search & Filter Header
          Container(
            padding: const EdgeInsets.all(16),
            color: AppColors.surface,
            child: Column(
              children: [
                TextField(
                  controller: _searchController,
                  decoration: InputDecoration(
                    hintText: 'Search by student name or phone...',
                    prefixIcon: const Icon(Icons.search_rounded, color: AppColors.textSecondary),
                    suffixIcon: _searchController.text.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear_rounded, size: 20),
                            onPressed: () {
                              _searchController.clear();
                              setState(() => _searchQuery = '');
                            },
                          )
                        : null,
                    filled: true,
                    fillColor: AppColors.background,
                    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: const BorderSide(color: AppColors.border),
                    ),
                  ),
                  onChanged: (val) {
                    setState(() => _searchQuery = val.trim());
                  },
                ),
                const SizedBox(height: 12),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: [
                      _buildFilterChip('ALL', 'All'),
                      const SizedBox(width: 8),
                      _buildFilterChip('ACTIVE', 'Active'),
                      const SizedBox(width: 8),
                      _buildFilterChip('OVERDUE', 'Overdue'),
                      const SizedBox(width: 8),
                      _buildFilterChip('ATTENDANCE_BLOCKED', 'Blocked'),
                      const SizedBox(width: 8),
                      _buildFilterChip('SUSPENDED', 'Suspended'),
                    ],
                  ),
                ),
              ],
            ),
          ),

          // Student List
          Expanded(
            child: studentsAsync.when(
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
                      Text('Error loading students: $err', textAlign: TextAlign.center),
                      const SizedBox(height: 12),
                      ElevatedButton(
                        onPressed: () => ref.invalidate(studentDirectoryProvider(filter)),
                        child: const Text('Retry'),
                      ),
                    ],
                  ),
                ),
              ),
              data: (students) {
                if (students.isEmpty) {
                  return Center(
                    child: Padding(
                      padding: const EdgeInsets.all(32),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(Icons.people_outline_rounded, size: 48, color: AppColors.textSecondary.withOpacity(0.5)),
                          const SizedBox(height: 12),
                          const Text(
                            'No students match the current filter.',
                            style: TextStyle(color: AppColors.textSecondary, fontSize: 14),
                          ),
                        ],
                      ),
                    ),
                  );
                }

                return ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: students.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 12),
                  itemBuilder: (context, index) {
                    final student = students[index];
                    return _buildStudentCard(student);
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterChip(String value, String label) {
    final isSelected = _selectedStatus == value;
    return ChoiceChip(
      label: Text(label),
      selected: isSelected,
      selectedColor: AppColors.primary,
      backgroundColor: AppColors.surfaceLight,
      labelStyle: TextStyle(
        color: isSelected ? Colors.white : AppColors.textSecondary,
        fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
        fontSize: 12,
      ),
      onSelected: (selected) {
        if (selected) {
          setState(() => _selectedStatus = value);
        }
      },
    );
  }

  Widget _buildStudentCard(AdminStudentModel student) {
    Color statusColor = AppColors.success;
    if (student.isBlocked) {
      statusColor = AppColors.error;
    } else if (student.isOverdue) {
      statusColor = AppColors.warning;
    } else if (student.isSuspended) {
      statusColor = AppColors.textSecondary;
    }

    return Card(
      color: AppColors.surface,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(
          color: student.isBlocked ? AppColors.error.withOpacity(0.5) : AppColors.border,
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                CircleAvatar(
                  backgroundColor: AppColors.primary.withOpacity(0.15),
                  child: Text(
                    student.fullName.isNotEmpty ? student.fullName[0].toUpperCase() : 'S',
                    style: const TextStyle(color: AppColors.primary, fontWeight: FontWeight.bold),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        student.fullName,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        student.phoneNumber,
                        style: const TextStyle(color: AppColors.textSecondary, fontSize: 13),
                      ),
                    ],
                  ),
                ),
                // Seat Number Badge
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.surfaceLight,
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: AppColors.primary.withOpacity(0.4)),
                  ),
                  child: Text(
                    'Seat ${student.seatNumber}',
                    style: const TextStyle(
                      color: AppColors.primary,
                      fontWeight: FontWeight.bold,
                      fontSize: 12,
                    ),
                  ),
                ),
              ],
            ),
            const Divider(height: 24),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                // Subscription Status
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: statusColor.withOpacity(0.12),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Text(
                    student.isSuspended ? 'SUSPENDED' : (student.subscriptionStatus ?? 'NO_PLAN'),
                    style: TextStyle(color: statusColor, fontWeight: FontWeight.bold, fontSize: 11),
                  ),
                ),
                if (student.dueDate != null)
                  Text(
                    'Due: ${Formatters.formatDate(student.dueDate!)}',
                    style: const TextStyle(color: AppColors.textSecondary, fontSize: 12),
                  ),
              ],
            ),
            const SizedBox(height: 12),
            // Action Buttons
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                OutlinedButton.icon(
                  icon: const Icon(Icons.currency_rupee_rounded, size: 16),
                  label: const Text('Record Cash', style: TextStyle(fontSize: 12)),
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                  onPressed: () => _showRecordCashDialog(student),
                ),
                const SizedBox(width: 8),
                PopupMenuButton<String>(
                  icon: const Icon(Icons.more_vert_rounded, color: AppColors.textSecondary),
                  onSelected: (val) {
                    if (val == 'TOGGLE_STATUS') {
                      _toggleStudentStatus(student);
                    } else if (val == 'RELEASE_SEAT' && student.seatId != null) {
                      _releaseStudentSeat(student);
                    }
                  },
                  itemBuilder: (ctx) => [
                    PopupMenuItem(
                      value: 'TOGGLE_STATUS',
                      child: Text(student.isSuspended ? 'Reactivate Student' : 'Suspend Student'),
                    ),
                    if (student.seatId != null)
                      const PopupMenuItem(
                        value: 'RELEASE_SEAT',
                        child: Text('Release Seat', style: TextStyle(color: AppColors.error)),
                      ),
                  ],
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _showRecordCashDialog(AdminStudentModel student) {
    final amountController = TextEditingController(text: '700');
    final notesController = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('Record Cash: ${student.fullName}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: amountController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Amount (₹)',
                prefixIcon: Icon(Icons.currency_rupee_rounded),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: notesController,
              decoration: const InputDecoration(
                labelText: 'Notes (Optional receipt memo)',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              final amount = double.tryParse(amountController.text) ?? 700.0;
              Navigator.pop(ctx);
              final success = await ref.read(adminActionProvider.notifier).recordCashPayment(
                    studentId: student.studentId,
                    amount: amount,
                    notes: notesController.text,
                  );
              if (mounted && success) {
                ref.invalidate(studentDirectoryProvider);
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    backgroundColor: AppColors.success,
                    content: Text('Cash payment recorded for ${student.fullName}!'),
                  ),
                );
              }
            },
            child: const Text('Save Payment'),
          ),
        ],
      ),
    );
  }

  void _toggleStudentStatus(AdminStudentModel student) async {
    final newStatus = student.isSuspended ? 'ACTIVE' : 'SUSPENDED';
    try {
      final service = ref.read(adminServiceProvider);
      await service.updateStudentStatus(studentId: student.studentId, status: newStatus);
      if (mounted) {
        ref.invalidate(studentDirectoryProvider);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: AppColors.success,
            content: Text('${student.fullName} status updated to $newStatus'),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(backgroundColor: AppColors.error, content: Text('Failed: $e')),
        );
      }
    }
  }

  void _releaseStudentSeat(AdminStudentModel student) async {
    if (student.seatId == null) return;
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Release Seat?'),
        content: Text('Are you sure you want to release Seat ${student.seatNumber} occupied by ${student.fullName}?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancel')),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.error),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Release'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      final success = await ref.read(adminActionProvider.notifier).releaseSeat(
            student.seatId!,
            reason: 'Released via Student Directory',
          );
      if (mounted && success) {
        ref.invalidate(studentDirectoryProvider);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: AppColors.success,
            content: Text('Seat ${student.seatNumber} released!'),
          ),
        );
      }
    }
  }
}
