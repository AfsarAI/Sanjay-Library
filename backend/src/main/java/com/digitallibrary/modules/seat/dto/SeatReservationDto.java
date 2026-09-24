package com.digitallibrary.modules.seat.dto;

import com.digitallibrary.modules.seat.SeatReservation;
import com.digitallibrary.modules.seat.SeatReservationStatus;

import java.time.Instant;

public class SeatReservationDto {

    private Long id;
    private Long libraryId;
    private Long seatId;
    private String seatNumber;
    private Long userId;
    private SeatReservationStatus status;
    private Instant expiresAt;

    public SeatReservationDto() {
    }

    public static SeatReservationDto fromEntity(SeatReservation reservation, String seatNumber) {
        SeatReservationDto dto = new SeatReservationDto();
        dto.setId(reservation.getId());
        dto.setLibraryId(reservation.getLibraryId());
        dto.setSeatId(reservation.getSeatId());
        dto.setSeatNumber(seatNumber);
        dto.setUserId(reservation.getUserId());
        dto.setStatus(reservation.getStatus());
        dto.setExpiresAt(reservation.getExpiresAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Long libraryId) {
        this.libraryId = libraryId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public SeatReservationStatus getStatus() {
        return status;
    }

    public void setStatus(SeatReservationStatus status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
