class UserModel {
  final int id;
  final int libraryId;
  final String phoneNumber;
  final String? email;
  final String fullName;
  final String role; // ROLE_STUDENT, ROLE_ADMIN
  final String status; // ACTIVE, INACTIVE, SUSPENDED

  const UserModel({
    required this.id,
    required this.libraryId,
    required this.phoneNumber,
    this.email,
    required this.fullName,
    required this.role,
    required this.status,
  });

  bool get isAdmin => role == 'ROLE_ADMIN';
  bool get isStudent => role == 'ROLE_STUDENT';

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'] as int,
      libraryId: json['libraryId'] as int? ?? 1,
      phoneNumber: json['phoneNumber'] as String? ?? '',
      email: json['email'] as String?,
      fullName: json['fullName'] as String? ?? 'User',
      role: json['role'] as String? ?? 'ROLE_STUDENT',
      status: json['status'] as String? ?? 'ACTIVE',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'libraryId': libraryId,
      'phoneNumber': phoneNumber,
      'email': email,
      'fullName': fullName,
      'role': role,
      'status': status,
    };
  }
}
