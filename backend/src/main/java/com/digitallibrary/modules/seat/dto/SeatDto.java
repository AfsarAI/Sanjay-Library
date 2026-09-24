package com.digitallibrary.modules.seat.dto;

import com.digitallibrary.modules.seat.Seat;
import com.digitallibrary.modules.seat.SeatStatus;

public class SeatDto {

    private Long id;
    private Long libraryId;
    private String seatNumber;
    private Integer rowNumber;
    private Integer colNumber;
    private SeatStatus status;

    public SeatDto() {
    }

    public static SeatDto fromEntity(Seat seat) {
        SeatDto dto = new SeatDto();
        dto.setId(seat.getId());
        dto.setLibraryId(seat.getLibraryId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setRowNumber(seat.getRowNumber());
        dto.setColNumber(seat.getColNumber());
        dto.setStatus(seat.getStatus());
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

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getColNumber() {
        return colNumber;
    }

    public void setColNumber(Integer colNumber) {
        this.colNumber = colNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }
}
