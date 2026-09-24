import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/constants/app_colors.dart';
import '../../models/seat_model.dart';
import '../admin/admin_provider.dart';
import '../authentication/auth_provider.dart';
import 'seat_provider.dart';
import 'widgets/seat_box.dart';

class SeatLayoutScreen extends ConsumerStatefulWidget {
  const SeatLayoutScreen({super.key});

  @override
  ConsumerState<SeatLayoutScreen> createState() => _SeatLayoutScreenState();
}

class _SeatLayoutScreenState extends ConsumerState<SeatLayoutScreen> {
  @override
  Widget build(BuildContext context) {
    final layoutAsync = ref.watch(seatLayoutProvider);
    final selectedSeat = ref.watch(selectedSeatProvider);
    final authState = ref.watch(authNotifierProvider);
    final isAdmin = authState.isAdmin;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Library Desks (A01 - A50)'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            tooltip: 'Refresh Desks',
            onPressed: () => ref.invalidate(seatLayoutProvider),
          ),
        ],
      ),
      body: layoutAsync.when(
        loading: () => const Center(
          child: CircularProgressIndicator(color: AppColors.primary),
        ),
        error: (err, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline_rounded, size: 48, color: AppColors.error),
              const SizedBox(height: 12),
              Text(
                'Failed to load seat layout',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              const SizedBox(height: 8),
              Text(
                err.toString(),
                style: Theme.of(context).textTheme.bodyMedium,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 16),
              ElevatedButton.icon(
                onPressed: () => ref.invalidate(seatLayoutProvider),
                icon: const Icon(Icons.refresh_rounded),
                label: const Text('Retry'),
              ),
            ],
          ),
        ),
        data: (layout) {
          return Column(
            children: [
              // Top Metrics Bar
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                color: AppColors.surface,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    _buildStatItem('Total', '${layout.totalSeats}', AppColors.textPrimary),
                    _buildStatItem('Available', '${layout.availableSeats}', AppColors.seatAvailable),
                    _buildStatItem('Occupied', '${layout.occupiedSeats}', AppColors.seatOccupied),
                    _buildStatItem('Reserved', '${layout.reservedSeats}', AppColors.seatReserved),
                  ],
                ),
              ),

              // Status Legend Bar
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                color: AppColors.background,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    _buildLegendItem('Available', AppColors.seatAvailable),
                    _buildLegendItem('Occupied', AppColors.seatOccupied),
                    _buildLegendItem('Reserved', AppColors.seatReserved),
                    _buildLegendItem('Selected', AppColors.seatSelected),
                  ],
                ),
              ),

              const Divider(height: 1, color: AppColors.border),

              // 2D Desks Grid
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: GridView.builder(
                    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 5,
                      mainAxisSpacing: 10,
                      crossAxisSpacing: 10,
                      childAspectRatio: 1.15,
                    ),
                    itemCount: layout.seats.length,
                    itemBuilder: (context, index) {
                      final seat = layout.seats[index];
                      final isSelected = selectedSeat?.id == seat.id;

                      return SeatBox(
                        seat: seat,
                        isSelected: isSelected,
                        onTap: () {
                          ref.read(selectedSeatProvider.notifier).state = seat;
                          _showSeatDetailSheet(context, seat, isAdmin);
                        },
                      );
                    },
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildStatItem(String label, String value, Color color) {
    return Column(
      children: [
        Text(
          value,
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.w800,
            color: color,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          label,
          style: const TextStyle(fontSize: 12, color: AppColors.textSecondary),
        ),
      ],
    );
  }

  Widget _buildLegendItem(String label, Color color) {
    return Row(
      children: [
        Container(
          width: 10,
          height: 10,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
          ),
        ),
        const SizedBox(width: 5),
        Text(
          label,
          style: const TextStyle(fontSize: 11, color: AppColors.textSecondary),
        ),
      ],
    );
  }

  void _showSeatDetailSheet(BuildContext context, SeatModel seat, bool isAdmin) {
    showModalBottomSheet(
      context: context,
      backgroundColor: AppColors.surface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (bottomSheetContext) {
        return Padding(
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 28),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.border,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: AppColors.primary.withOpacity(0.15),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Icon(Icons.event_seat_rounded, color: AppColors.primary, size: 28),
                      ),
                      const SizedBox(width: 12),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Desk ${seat.seatNumber}',
                            style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                          ),
                          Text(
                            'Row ${seat.rowNumber}, Col ${seat.colNumber}',
                            style: const TextStyle(color: AppColors.textSecondary, fontSize: 13),
                          ),
                        ],
                      ),
                    ],
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: AppColors.surfaceLight,
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      seat.status.displayName,
                      style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 12),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 20),

              // Action buttons based on status and role
              if (seat.isAvailable) ...[
                const Text(
                  'This desk is currently free. You can reserve it for 10 minutes to complete your admission.',
                  style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
                ),
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    icon: const Icon(Icons.check_circle_outline_rounded),
                    label: const Text('Reserve Desk & Apply for Admission'),
                    onPressed: () async {
                      Navigator.pop(bottomSheetContext);
                      final success = await ref
                          .read(reservationNotifierProvider.notifier)
                          .reserveSeat(seat.id);

                      if (context.mounted && success) {
                        context.push('/student/admission', extra: seat);
                      } else if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('Failed to reserve desk. It may have just been booked.')),
                        );
                      }
                    },
                  ),
                ),
              ] else if (seat.isOccupied && isAdmin) ...[
                const Text(
                  'This desk is currently assigned to a student.',
                  style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
                ),
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  child: OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.error,
                      side: const BorderSide(color: AppColors.error),
                    ),
                    icon: const Icon(Icons.delete_outline_rounded),
                    label: const Text('Admin: Release Seat to Available'),
                    onPressed: () async {
                      Navigator.pop(bottomSheetContext);
                      final confirmed = await showDialog<bool>(
                        context: context,
                        builder: (dCtx) => AlertDialog(
                          title: const Text('Release Seat?'),
                          content: Text('Are you sure you want to release desk ${seat.seatNumber}? This will unassign the current student.'),
                          actions: [
                            TextButton(onPressed: () => Navigator.pop(dCtx, false), child: const Text('Cancel')),
                            ElevatedButton(
                              style: ElevatedButton.styleFrom(backgroundColor: AppColors.error),
                              onPressed: () => Navigator.pop(dCtx, true),
                              child: const Text('Release'),
                            ),
                          ],
                        ),
                      );

                      if (confirmed == true) {
                        await ref.read(adminActionProvider.notifier).releaseSeat(seat.id);
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text('Desk ${seat.seatNumber} released successfully')),
                          );
                        }
                      }
                    },
                  ),
                ),
              ] else ...[
                Text(
                  seat.isReserved
                      ? 'This desk is currently on a 10-minute hold by another student.'
                      : 'This desk is occupied or under maintenance.',
                  style: const TextStyle(color: AppColors.textSecondary, fontSize: 13),
                ),
              ],
            ],
          ),
        );
      },
    );
  }
}
