package com.digitallibrary.modules.admin.dto;

import com.digitallibrary.modules.subscription.SubscriptionStatus;
import com.digitallibrary.modules.user.UserStatus;

import java.time.Instant;
import java.time.LocalDate;

public class AdminStudentDirectoryDto {

    private Long studentId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String seatNumber;
    private Long seatId;
    private UserStatus accountStatus;
    private SubscriptionStatus subscriptionStatus;
    private LocalDate dueDate;
    private LocalDate graceUntil;
    private Instant joinedAt;

    public AdminStudentDirectoryDto() {
    }

    public AdminStudentDirectoryDto(Long studentId, String fullName, String phoneNumber, String email,
                                    String seatNumber, Long seatId, UserStatus accountStatus,
                                    SubscriptionStatus subscriptionStatus, LocalDate dueDate,
                                    LocalDate graceUntil, Instant joinedAt) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.seatNumber = seatNumber;
        this.seatId = seatId;
        this.accountStatus = accountStatus;
        this.subscriptionStatus = subscriptionStatus;
        this.dueDate = dueDate;
        this.graceUntil = graceUntil;
        this.joinedAt = joinedAt;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public UserStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(UserStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
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

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
