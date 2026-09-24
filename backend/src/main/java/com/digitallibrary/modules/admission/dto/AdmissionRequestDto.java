package com.digitallibrary.modules.admission.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class AdmissionRequestDto {

    @NotNull(message = "Library ID is required")
    private Long libraryId;

    @NotNull(message = "Seat ID is required")
    private Long seatId;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private String emergencyContact;

    public AdmissionRequestDto() {
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

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
