class AdminDashboardModel {
  final int totalSeats;
  final int occupiedSeats;
  final int availableSeats;
  final int reservedSeats;
  final int maintenanceSeats;
  final int todayAttendanceCount;
  final int currentlyInsideCount;
  final int feesDueCount;
  final int overdueCount;
  final double monthlyRevenue;
  final double pendingRevenue;

  const AdminDashboardModel({
    required this.totalSeats,
    required this.occupiedSeats,
    required this.availableSeats,
    required this.reservedSeats,
    required this.maintenanceSeats,
    required this.todayAttendanceCount,
    required this.currentlyInsideCount,
    required this.feesDueCount,
    required this.overdueCount,
    required this.monthlyRevenue,
    required this.pendingRevenue,
  });

  factory AdminDashboardModel.fromJson(Map<String, dynamic> json) {
    return AdminDashboardModel(
      totalSeats: json['totalSeats'] as int? ?? 50,
      occupiedSeats: json['occupiedSeats'] as int? ?? 0,
      availableSeats: json['availableSeats'] as int? ?? 0,
      reservedSeats: json['reservedSeats'] as int? ?? 0,
      maintenanceSeats: json['maintenanceSeats'] as int? ?? 0,
      todayAttendanceCount: json['todayAttendanceCount'] as int? ?? 0,
      currentlyInsideCount: json['currentlyInsideCount'] as int? ?? 0,
      feesDueCount: json['feesDueCount'] as int? ?? 0,
      overdueCount: json['overdueCount'] as int? ?? 0,
      monthlyRevenue: (json['monthlyRevenue'] as num?)?.toDouble() ?? 0.0,
      pendingRevenue: (json['pendingRevenue'] as num?)?.toDouble() ?? 0.0,
    );
  }
}
