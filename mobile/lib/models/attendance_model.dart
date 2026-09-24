class AttendanceModel {
  final int id;
  final int studentId;
  final String? seatNumber;
  final DateTime attendanceDate;
  final DateTime checkInTime;
  final DateTime? checkOutTime;
  final int? durationMinutes;
  final String status; // CHECKED_IN, CHECKED_OUT, AUTO_CHECKOUT

  const AttendanceModel({
    required this.id,
    required this.studentId,
    this.seatNumber,
    required this.attendanceDate,
    required this.checkInTime,
    this.checkOutTime,
    this.durationMinutes,
    required this.status,
  });

  bool get isCurrentlyCheckedIn => status == 'CHECKED_IN' && checkOutTime == null;

  factory AttendanceModel.fromJson(Map<String, dynamic> json) {
    return AttendanceModel(
      id: json['id'] as int? ?? 0,
      studentId: json['studentId'] as int? ?? 0,
      seatNumber: json['seatNumber'] as String?,
      attendanceDate: DateTime.tryParse(json['attendanceDate'] as String? ?? '') ?? DateTime.now(),
      checkInTime: DateTime.tryParse(json['checkInTime'] as String? ?? '') ?? DateTime.now(),
      checkOutTime: json['checkOutTime'] != null
          ? DateTime.tryParse(json['checkOutTime'] as String)
          : null,
      durationMinutes: json['durationMinutes'] as int?,
      status: json['status'] as String? ?? 'CHECKED_IN',
    );
  }
}
