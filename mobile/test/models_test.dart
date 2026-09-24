import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/models/admin_dashboard_model.dart';
import 'package:mobile/models/attendance_model.dart';
import 'package:mobile/models/payment_model.dart';
import 'package:mobile/models/seat_model.dart';
import 'package:mobile/models/subscription_model.dart';
import 'package:mobile/models/user_model.dart';

void main() {
  group('Mobile Models Serialization & Logic Tests', () {
    test('UserModel correctly parses roles and admin privileges', () {
      final admin = UserModel.fromJson({
        'id': 1,
        'libraryId': 1,
        'phoneNumber': '9876543210',
        'fullName': 'Owner',
        'role': 'ROLE_ADMIN',
        'status': 'ACTIVE',
      });

      expect(admin.isAdmin, isTrue);
      expect(admin.isStudent, isFalse);
      expect(admin.phoneNumber, '9876543210');

      final student = UserModel.fromJson({
        'id': 2,
        'libraryId': 1,
        'phoneNumber': '9123456780',
        'fullName': 'Student',
        'role': 'ROLE_STUDENT',
        'status': 'ACTIVE',
      });

      expect(student.isAdmin, isFalse);
      expect(student.isStudent, isTrue);
    });

    test('SeatModel & SeatLayoutModel parse status and availability', () {
      final layout = SeatLayoutModel.fromJson({
        'libraryId': 1,
        'totalSeats': 50,
        'availableSeats': 40,
        'occupiedSeats': 8,
        'reservedSeats': 2,
        'maintenanceSeats': 0,
        'seats': [
          {'id': 1, 'seatNumber': 'A01', 'rowNumber': 1, 'colNumber': 1, 'status': 'AVAILABLE'},
          {'id': 2, 'seatNumber': 'A02', 'rowNumber': 1, 'colNumber': 2, 'status': 'OCCUPIED'},
          {'id': 3, 'seatNumber': 'A03', 'rowNumber': 1, 'colNumber': 3, 'status': 'RESERVED'},
        ],
      });

      expect(layout.totalSeats, 50);
      expect(layout.seats.length, 3);
      expect(layout.seats[0].isAvailable, isTrue);
      expect(layout.seats[1].isOccupied, isTrue);
      expect(layout.seats[2].isReserved, isTrue);
    });

    test('SubscriptionModel evaluates overdue and attendance block rules', () {
      final activeSub = SubscriptionModel.fromJson({
        'id': 10,
        'studentId': 2,
        'seatNumber': 'A23',
        'cycleStartDate': '2026-09-15',
        'cycleEndDate': '2026-10-14',
        'dueDate': '2026-10-15',
        'monthlyFee': 700.0,
        'status': 'ACTIVE',
        'daysOverdue': 0,
        'isAttendanceAllowed': true,
      });

      expect(activeSub.isActive, isTrue);
      expect(activeSub.isBlocked, isFalse);
      expect(activeSub.monthlyFee, 700.0);

      final blockedSub = SubscriptionModel.fromJson({
        'id': 11,
        'studentId': 3,
        'seatNumber': 'A05',
        'cycleStartDate': '2026-08-15',
        'cycleEndDate': '2026-09-14',
        'dueDate': '2026-09-15',
        'monthlyFee': 700.0,
        'status': 'ATTENDANCE_BLOCKED',
        'daysOverdue': 9,
        'isAttendanceAllowed': false,
      });

      expect(blockedSub.isBlocked, isTrue);
      expect(blockedSub.isOverdue, isTrue);
      expect(blockedSub.daysOverdue, 9);
    });

    test('AttendanceModel evaluates active study sessions', () {
      final activeSession = AttendanceModel.fromJson({
        'id': 100,
        'studentId': 2,
        'seatNumber': 'A23',
        'attendanceDate': '2026-09-24',
        'checkInTime': '2026-09-24T08:30:00Z',
        'checkOutTime': null,
        'durationMinutes': null,
        'status': 'CHECKED_IN',
      });

      expect(activeSession.isCurrentlyCheckedIn, isTrue);

      final completedSession = AttendanceModel.fromJson({
        'id': 101,
        'studentId': 2,
        'seatNumber': 'A23',
        'attendanceDate': '2026-09-23',
        'checkInTime': '2026-09-23T08:00:00Z',
        'checkOutTime': '2026-09-23T14:30:00Z',
        'durationMinutes': 390,
        'status': 'CHECKED_OUT',
      });

      expect(completedSession.isCurrentlyCheckedIn, isFalse);
      expect(completedSession.durationMinutes, 390);
    });

    test('PaymentModel evaluates method and status', () {
      final razorpayPayment = PaymentModel.fromJson({
        'id': 50,
        'orderId': 'order_12345',
        'amount': 700.0,
        'paymentMethod': 'RAZORPAY',
        'status': 'SUCCESS',
        'paidAt': '2026-09-24T10:00:00Z',
        'receiptNumber': 'REC-2026-0001',
      });

      expect(razorpayPayment.isSuccess, isTrue);
      expect(razorpayPayment.isCash, isFalse);
      expect(razorpayPayment.receiptNumber, 'REC-2026-0001');

      final cashPayment = PaymentModel.fromJson({
        'id': 51,
        'orderId': 'CASH_REC_999',
        'amount': 700.0,
        'paymentMethod': 'CASH',
        'status': 'SUCCESS',
        'paidAt': '2026-09-24T11:00:00Z',
      });

      expect(cashPayment.isSuccess, isTrue);
      expect(cashPayment.isCash, isTrue);
    });

    test('AdminDashboardModel maps counts correctly', () {
      final metrics = AdminDashboardModel.fromJson({
        'totalSeats': 50,
        'occupiedSeats': 42,
        'availableSeats': 6,
        'reservedSeats': 2,
        'maintenanceSeats': 0,
        'todayAttendanceCount': 37,
        'currentlyInsideCount': 24,
        'feesDueCount': 3,
        'overdueCount': 1,
        'monthlyRevenue': 29400.0,
        'pendingRevenue': 2100.0,
      });

      expect(metrics.totalSeats, 50);
      expect(metrics.occupiedSeats, 42);
      expect(metrics.currentlyInsideCount, 24);
      expect(metrics.monthlyRevenue, 29400.0);
    });
  });
}
