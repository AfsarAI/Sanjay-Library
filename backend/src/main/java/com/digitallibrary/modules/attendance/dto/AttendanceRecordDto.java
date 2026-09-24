package com.digitallibrary.modules.attendance.dto;

import com.digitallibrary.modules.attendance.AttendanceRecord;
import com.digitallibrary.modules.attendance.AttendanceStatus;
import com.digitallibrary.modules.attendance.AttendanceVerificationMethod;

import java.time.Instant;
import java.time.LocalDate;

public class AttendanceRecordDto {

    private Long id;
    private Long libraryId;
    private Long studentId;
    private String studentName;
    private Long seatId;
    private String seatNumber;
    private LocalDate date;
    private Instant checkInTime;
    private Instant checkOutTime;
    private Integer durationMinutes;
    private AttendanceStatus status;
    private AttendanceVerificationMethod verificationMethod;
    private String notes;

    public AttendanceRecordDto() {
    }

    public static AttendanceRecordDto fromEntity(AttendanceRecord record, String studentName, String seatNumber) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setId(record.getId());
        dto.setLibraryId(record.getLibraryId());
        dto.setStudentId(record.getStudentId());
        dto.setStudentName(studentName);
        dto.setSeatId(record.getSeatId());
        dto.setSeatNumber(seatNumber);
        dto.setDate(record.getDate());
        dto.setCheckInTime(record.getCheckInTime());
        dto.setCheckOutTime(record.getCheckOutTime());
        dto.setDurationMinutes(record.getDurationMinutes());
        dto.setStatus(record.getStatus());
        dto.setVerificationMethod(record.getVerificationMethod());
        dto.setNotes(record.getNotes());
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Instant getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Instant checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Instant getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Instant checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public AttendanceVerificationMethod getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(AttendanceVerificationMethod verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
