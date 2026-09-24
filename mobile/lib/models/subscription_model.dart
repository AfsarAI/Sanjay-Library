class SubscriptionModel {
  final int id;
  final int studentId;
  final String? seatNumber;
  final DateTime cycleStartDate;
  final DateTime cycleEndDate;
  final DateTime dueDate;
  final double monthlyFee;
  final String status;
  final int daysOverdue;
  final bool isAttendanceAllowed;

  const SubscriptionModel({
    required this.id,
    required this.studentId,
    this.seatNumber,
    required this.cycleStartDate,
    required this.cycleEndDate,
    required this.dueDate,
    required this.monthlyFee,
    required this.status,
    required this.daysOverdue,
    required this.isAttendanceAllowed,
  });

  bool get isActive => status == 'ACTIVE';
  bool get isPaymentDue => status == 'PAYMENT_DUE';
  bool get isOverdue => status == 'OVERDUE' || daysOverdue > 0;
  bool get isGracePeriod => status == 'GRACE_PERIOD';
  bool get isBlocked => status == 'ATTENDANCE_BLOCKED' || !isAttendanceAllowed;

  factory SubscriptionModel.fromJson(Map<String, dynamic> json) {
    return SubscriptionModel(
      id: json['id'] as int? ?? 0,
      studentId: json['studentId'] as int? ?? 0,
      seatNumber: json['seatNumber'] as String?,
      cycleStartDate: DateTime.tryParse(json['cycleStartDate'] as String? ?? '') ?? DateTime.now(),
      cycleEndDate: DateTime.tryParse(json['cycleEndDate'] as String? ?? '') ?? DateTime.now(),
      dueDate: DateTime.tryParse(json['dueDate'] as String? ?? '') ?? DateTime.now(),
      monthlyFee: (json['monthlyFee'] as num?)?.toDouble() ?? 700.0,
      status: json['status'] as String? ?? 'ACTIVE',
      daysOverdue: json['daysOverdue'] as int? ?? 0,
      isAttendanceAllowed: json['isAttendanceAllowed'] as bool? ?? true,
    );
  }
}
