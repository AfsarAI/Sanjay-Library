class AdminStudentModel {
  final int studentId;
  final String fullName;
  final String phoneNumber;
  final String? email;
  final String seatNumber;
  final int? seatId;
  final String accountStatus;
  final String? subscriptionStatus;
  final DateTime? dueDate;
  final DateTime? graceUntil;
  final DateTime joinedAt;

  AdminStudentModel({
    required this.studentId,
    required this.fullName,
    required this.phoneNumber,
    this.email,
    required this.seatNumber,
    this.seatId,
    required this.accountStatus,
    this.subscriptionStatus,
    this.dueDate,
    this.graceUntil,
    required this.joinedAt,
  });

  bool get isBlocked => subscriptionStatus == 'ATTENDANCE_BLOCKED';
  bool get isOverdue => subscriptionStatus == 'OVERDUE';
  bool get isActive => subscriptionStatus == 'ACTIVE';
  bool get isSuspended => accountStatus == 'SUSPENDED';

  factory AdminStudentModel.fromJson(Map<String, dynamic> json) {
    return AdminStudentModel(
      studentId: json['studentId'] as int,
      fullName: json['fullName'] as String? ?? 'Student',
      phoneNumber: json['phoneNumber'] as String? ?? '',
      email: json['email'] as String?,
      seatNumber: json['seatNumber'] as String? ?? 'None',
      seatId: json['seatId'] as int?,
      accountStatus: json['accountStatus'] as String? ?? 'ACTIVE',
      subscriptionStatus: json['subscriptionStatus'] as String?,
      dueDate: json['dueDate'] != null ? DateTime.tryParse(json['dueDate'] as String) : null,
      graceUntil: json['graceUntil'] != null ? DateTime.tryParse(json['graceUntil'] as String) : null,
      joinedAt: json['joinedAt'] != null
          ? DateTime.tryParse(json['joinedAt'] as String) ?? DateTime.now()
          : DateTime.now(),
    );
  }
}
