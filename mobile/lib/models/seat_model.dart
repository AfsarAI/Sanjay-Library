enum SeatStatus {
  available,
  occupied,
  reserved,
  maintenance,
  unknown;

  static SeatStatus fromString(String? status) {
    switch (status?.toUpperCase()) {
      case 'AVAILABLE':
        return SeatStatus.available;
      case 'OCCUPIED':
        return SeatStatus.occupied;
      case 'RESERVED':
        return SeatStatus.reserved;
      case 'MAINTENANCE':
        return SeatStatus.maintenance;
      default:
        return SeatStatus.unknown;
    }
  }

  String get displayName {
    switch (this) {
      case SeatStatus.available:
        return 'Available';
      case SeatStatus.occupied:
        return 'Occupied';
      case SeatStatus.reserved:
        return 'Reserved';
      case SeatStatus.maintenance:
        return 'Maintenance';
      case SeatStatus.unknown:
        return 'Unknown';
    }
  }
}

class SeatModel {
  final int id;
  final int libraryId;
  final String seatNumber;
  final int rowNumber;
  final int colNumber;
  final SeatStatus status;

  const SeatModel({
    required this.id,
    required this.libraryId,
    required this.seatNumber,
    required this.rowNumber,
    required this.colNumber,
    required this.status,
  });

  bool get isAvailable => status == SeatStatus.available;
  bool get isOccupied => status == SeatStatus.occupied;
  bool get isReserved => status == SeatStatus.reserved;
  bool get isMaintenance => status == SeatStatus.maintenance;

  factory SeatModel.fromJson(Map<String, dynamic> json) {
    return SeatModel(
      id: json['id'] as int,
      libraryId: json['libraryId'] as int? ?? 1,
      seatNumber: json['seatNumber'] as String? ?? '',
      rowNumber: json['rowNumber'] as int? ?? 1,
      colNumber: json['colNumber'] as int? ?? 1,
      status: SeatStatus.fromString(json['status'] as String?),
    );
  }
}

class SeatLayoutModel {
  final int libraryId;
  final int totalSeats;
  final int availableSeats;
  final int occupiedSeats;
  final int reservedSeats;
  final int maintenanceSeats;
  final List<SeatModel> seats;

  const SeatLayoutModel({
    required this.libraryId,
    required this.totalSeats,
    required this.availableSeats,
    required this.occupiedSeats,
    required this.reservedSeats,
    required this.maintenanceSeats,
    required this.seats,
  });

  factory SeatLayoutModel.fromJson(Map<String, dynamic> json) {
    final list = (json['seats'] as List<dynamic>?)
            ?.map((e) => SeatModel.fromJson(e as Map<String, dynamic>))
            .toList() ??
        [];

    return SeatLayoutModel(
      libraryId: json['libraryId'] as int? ?? 1,
      totalSeats: json['totalSeats'] as int? ?? 0,
      availableSeats: json['availableSeats'] as int? ?? 0,
      occupiedSeats: json['occupiedSeats'] as int? ?? 0,
      reservedSeats: json['reservedSeats'] as int? ?? 0,
      maintenanceSeats: json['maintenanceSeats'] as int? ?? 0,
      seats: list,
    );
  }
}
