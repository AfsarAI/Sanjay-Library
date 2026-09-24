import 'package:intl/intl.dart';

class Formatters {
  static final DateFormat _dateFormatter = DateFormat('dd MMM yyyy');
  static final DateFormat _dateTimeFormatter = DateFormat('dd MMM yyyy, hh:mm a');
  static final DateFormat _timeFormatter = DateFormat('hh:mm a');
  static final NumberFormat _currencyFormatter = NumberFormat.currency(
    symbol: '₹',
    decimalDigits: 0,
    locale: 'en_IN',
  );

  static String formatDate(DateTime? date) {
    if (date == null) return '-';
    return _dateFormatter.format(date);
  }

  static String formatDateTime(DateTime? dateTime) {
    if (dateTime == null) return '-';
    return _dateTimeFormatter.format(dateTime);
  }

  static String formatTime(DateTime? time) {
    if (time == null) return '-';
    return _timeFormatter.format(time);
  }

  static String formatCurrency(num? amount) {
    if (amount == null) return '₹0';
    return _currencyFormatter.format(amount);
  }

  static String formatDurationMinutes(int? minutes) {
    if (minutes == null || minutes <= 0) return '0m';
    final hours = minutes ~/ 60;
    final remainingMinutes = minutes % 60;
    if (hours > 0) {
      return '${hours}h ${remainingMinutes}m';
    }
    return '${remainingMinutes}m';
  }
}
