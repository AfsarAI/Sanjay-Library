class PaymentModel {
  final int id;
  final String orderId;
  final double amount;
  final String paymentMethod; // RAZORPAY, CASH
  final String status; // SUCCESS, PENDING, FAILED
  final DateTime? paidAt;
  final String? receiptNumber;

  const PaymentModel({
    required this.id,
    required this.orderId,
    required this.amount,
    required this.paymentMethod,
    required this.status,
    this.paidAt,
    this.receiptNumber,
  });

  bool get isSuccess => status == 'SUCCESS';
  bool get isCash => paymentMethod == 'CASH';

  factory PaymentModel.fromJson(Map<String, dynamic> json) {
    return PaymentModel(
      id: json['id'] as int? ?? 0,
      orderId: json['orderId'] as String? ?? '',
      amount: (json['amount'] as num?)?.toDouble() ?? 0.0,
      paymentMethod: json['paymentMethod'] as String? ?? 'RAZORPAY',
      status: json['status'] as String? ?? 'PENDING',
      paidAt: json['paidAt'] != null ? DateTime.tryParse(json['paidAt'] as String) : null,
      receiptNumber: json['receiptNumber'] as String?,
    );
  }
}
