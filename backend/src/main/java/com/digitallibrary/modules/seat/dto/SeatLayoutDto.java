package com.digitallibrary.modules.seat.dto;

import java.util.List;

public class SeatLayoutDto {

    private Long libraryId;
    private int totalSeats;
    private long availableSeats;
    private long occupiedSeats;
    private long reservedSeats;
    private long maintenanceSeats;
    private List<SeatDto> seats;

    public SeatLayoutDto() {
    }

    public SeatLayoutDto(Long libraryId, int totalSeats, long availableSeats, long occupiedSeats,
                         long reservedSeats, long maintenanceSeats, List<SeatDto> seats) {
        this.libraryId = libraryId;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.occupiedSeats = occupiedSeats;
        this.reservedSeats = reservedSeats;
        this.maintenanceSeats = maintenanceSeats;
        this.seats = seats;
    }

    public Long getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Long libraryId) {
        this.libraryId = libraryId;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public long getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(long availableSeats) {
        this.availableSeats = availableSeats;
    }

    public long getOccupiedSeats() {
        return occupiedSeats;
    }

    public void setOccupiedSeats(long occupiedSeats) {
        this.occupiedSeats = occupiedSeats;
    }

    public long getReservedSeats() {
        return reservedSeats;
    }

    public void setReservedSeats(long reservedSeats) {
        this.reservedSeats = reservedSeats;
    }

    public long getMaintenanceSeats() {
        return maintenanceSeats;
    }

    public void setMaintenanceSeats(long maintenanceSeats) {
        this.maintenanceSeats = maintenanceSeats;
    }

    public List<SeatDto> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDto> seats) {
        this.seats = seats;
    }
}
