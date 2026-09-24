package com.digitallibrary.modules.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SubscriptionDto {

    private Long id;
    private Long libraryId;
    private Long studentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate dueDate;
    private LocalDate graceUntil;
    private BigDecimal amount;
    private SubscriptionStatus status;
    private boolean isAttendanceEligible;

    public SubscriptionDto() {
    }

    public static SubscriptionDto fromEntity(Subscription sub) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(sub.getId());
        dto.setLibraryId(sub.getLibraryId());
        dto.setStudentId(sub.getStudentId());
        dto.setStartDate(sub.getStartDate());
        dto.setEndDate(sub.getEndDate());
        dto.setDueDate(sub.getDueDate());
        dto.setGraceUntil(sub.getGraceUntil());
        dto.setAmount(sub.getAmount());
        dto.setStatus(sub.getStatus());

        // Attendance is permitted if ACTIVE, PAYMENT_DUE, OVERDUE (within grace), or EXTENDED
        boolean eligible = sub.getStatus() == SubscriptionStatus.ACTIVE ||
                sub.getStatus() == SubscriptionStatus.PAYMENT_DUE ||
                sub.getStatus() == SubscriptionStatus.EXTENDED ||
                (sub.getStatus() == SubscriptionStatus.OVERDUE &&
                        sub.getGraceUntil() != null && !LocalDate.now().isAfter(sub.getGraceUntil()));

        dto.setAttendanceEligible(eligible);
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getGraceUntil() {
        return graceUntil;
    }

    public void setGraceUntil(LocalDate graceUntil) {
        this.graceUntil = graceUntil;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public boolean isAttendanceEligible() {
        return isAttendanceEligible;
    }

    public void setAttendanceEligible(boolean attendanceEligible) {
        isAttendanceEligible = attendanceEligible;
    }
}
