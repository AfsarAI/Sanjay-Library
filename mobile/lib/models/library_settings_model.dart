class LibrarySettingsModel {
  final int? id;
  final int libraryId;
  final double monthlyFeeAmount;
  final int gracePeriodDays;
  final int attendanceBlockAfterDays;
  final int seatReleaseAfterDays;
  final int reservationTimeoutMinutes;
  final bool allowQrAttendance;

  LibrarySettingsModel({
    this.id,
    required this.libraryId,
    required this.monthlyFeeAmount,
    required this.gracePeriodDays,
    required this.attendanceBlockAfterDays,
    required this.seatReleaseAfterDays,
    required this.reservationTimeoutMinutes,
    required this.allowQrAttendance,
  });

  factory LibrarySettingsModel.fromJson(Map<String, dynamic> json) {
    return LibrarySettingsModel(
      id: json['id'] as int?,
      libraryId: json['libraryId'] as int? ?? 1,
      monthlyFeeAmount: (json['monthlyFeeAmount'] as num?)?.toDouble() ?? 700.0,
      gracePeriodDays: json['gracePeriodDays'] as int? ?? 7,
      attendanceBlockAfterDays: json['attendanceBlockAfterDays'] as int? ?? 8,
      seatReleaseAfterDays: json['seatReleaseAfterDays'] as int? ?? 15,
      reservationTimeoutMinutes: json['reservationTimeoutMinutes'] as int? ?? 10,
      allowQrAttendance: json['allowQrAttendance'] as bool? ?? true,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'libraryId': libraryId,
      'monthlyFeeAmount': monthlyFeeAmount,
      'gracePeriodDays': gracePeriodDays,
      'attendanceBlockAfterDays': attendanceBlockAfterDays,
      'seatReleaseAfterDays': seatReleaseAfterDays,
      'reservationTimeoutMinutes': reservationTimeoutMinutes,
      'allowQrAttendance': allowQrAttendance,
    };
  }
}
