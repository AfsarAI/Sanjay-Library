package com.digitallibrary.modules.admission.dto;

import com.digitallibrary.modules.admission.Admission;
import com.digitallibrary.modules.admission.AdmissionStatus;
import com.digitallibrary.modules.subscription.SubscriptionDto;

import java.time.LocalDate;

public class AdmissionResponseDto {

    private Long id;
    private Long libraryId;
    private Long studentId;
    private String studentName;
    private String studentPhone;
    private Long seatId;
    private String seatNumber;
    private LocalDate joiningDate;
    private String emergencyContact;
    private AdmissionStatus status;
    private SubscriptionDto subscription;

    public AdmissionResponseDto() {
    }

    public static AdmissionResponseDto fromEntity(Admission admission, String studentName, String studentPhone, String seatNumber, SubscriptionDto subscription) {
        AdmissionResponseDto dto = new AdmissionResponseDto();
        dto.setId(admission.getId());
        dto.setLibraryId(admission.getLibraryId());
        dto.setStudentId(admission.getStudentId());
        dto.setStudentName(studentName);
        dto.setStudentPhone(studentPhone);
        dto.setSeatId(admission.getSeatId());
        dto.setSeatNumber(seatNumber);
        dto.setJoiningDate(admission.getJoiningDate());
        dto.setEmergencyContact(admission.getEmergencyContact());
        dto.setStatus(admission.getStatus());
        dto.setSubscription(subscription);
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

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
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

    public AdmissionStatus getStatus() {
        return status;
    }

    public void setStatus(AdmissionStatus status) {
        this.status = status;
    }

    public SubscriptionDto getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionDto subscription) {
        this.subscription = subscription;
    }
}
