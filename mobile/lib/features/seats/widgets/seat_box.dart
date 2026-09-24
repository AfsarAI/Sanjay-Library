import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';
import '../../../models/seat_model.dart';

class SeatBox extends StatelessWidget {
  final SeatModel seat;
  final bool isSelected;
  final VoidCallback? onTap;

  const SeatBox({
    super.key,
    required this.seat,
    this.isSelected = false,
    this.onTap,
  });

  Color _getStatusColor() {
    if (isSelected) return AppColors.seatSelected;
    switch (seat.status) {
      case SeatStatus.available:
        return AppColors.seatAvailable;
      case SeatStatus.occupied:
        return AppColors.seatOccupied;
      case SeatStatus.reserved:
        return AppColors.seatReserved;
      case SeatStatus.maintenance:
        return AppColors.seatMaintenance;
      default:
        return AppColors.surfaceLight;
    }
  }

  IconData _getStatusIcon() {
    switch (seat.status) {
      case SeatStatus.available:
        return Icons.event_seat_rounded;
      case SeatStatus.occupied:
        return Icons.person_rounded;
      case SeatStatus.reserved:
        return Icons.schedule_rounded;
      case SeatStatus.maintenance:
        return Icons.build_rounded;
      default:
        return Icons.event_seat_rounded;
    }
  }

  @override
  Widget build(BuildContext context) {
    final statusColor = _getStatusColor();

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(10),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          decoration: BoxDecoration(
            color: isSelected
                ? statusColor.withOpacity(0.25)
                : statusColor.withOpacity(0.12),
            borderRadius: BorderRadius.circular(10),
            border: Border.all(
              color: isSelected ? statusColor : statusColor.withOpacity(0.6),
              width: isSelected ? 2.2 : 1.2,
            ),
            boxShadow: isSelected
                ? [
                    BoxShadow(
                      color: statusColor.withOpacity(0.35),
                      blurRadius: 10,
                      spreadRadius: 1,
                    )
                  ]
                : [],
          ),
          padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 6),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                _getStatusIcon(),
                size: 20,
                color: statusColor,
              ),
              const SizedBox(height: 4),
              Text(
                seat.seatNumber,
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w700,
                  color: isSelected ? Colors.white : AppColors.textPrimary,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
