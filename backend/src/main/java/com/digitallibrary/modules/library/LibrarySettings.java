package com.digitallibrary.modules.library;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "library_settings")
public class LibrarySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "library_id", nullable = false, unique = true)
    private Long libraryId;

    @Column(name = "monthly_fee_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyFeeAmount = new BigDecimal("700.00");

    @Column(name = "grace_period_days", nullable = false)
    private Integer gracePeriodDays = 7;

    @Column(name = "attendance_block_after_days", nullable = false)
    private Integer attendanceBlockAfterDays = 8;

    @Column(name = "seat_release_after_days", nullable = false)
    private Integer seatReleaseAfterDays = 15;

    @Column(name = "reservation_timeout_minutes", nullable = false)
    private Integer reservationTimeoutMinutes = 10;

    @Column(name = "allow_qr_attendance", nullable = false)
    private Boolean allowQrAttendance = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    public LibrarySettings() {
    }

    public LibrarySettings(Long libraryId, BigDecimal monthlyFeeAmount, Integer gracePeriodDays,
                           Integer attendanceBlockAfterDays, Integer seatReleaseAfterDays,
                           Integer reservationTimeoutMinutes, Boolean allowQrAttendance) {
        this.libraryId = libraryId;
        this.monthlyFeeAmount = monthlyFeeAmount;
        this.gracePeriodDays = gracePeriodDays;
        this.attendanceBlockAfterDays = attendanceBlockAfterDays;
        this.seatReleaseAfterDays = seatReleaseAfterDays;
        this.reservationTimeoutMinutes = reservationTimeoutMinutes;
        this.allowQrAttendance = allowQrAttendance != null ? allowQrAttendance : true;
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
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

    public BigDecimal getMonthlyFeeAmount() {
        return monthlyFeeAmount;
    }

    public void setMonthlyFeeAmount(BigDecimal monthlyFeeAmount) {
        this.monthlyFeeAmount = monthlyFeeAmount;
    }

    public Integer getGracePeriodDays() {
        return gracePeriodDays;
    }

    public void setGracePeriodDays(Integer gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }

    public Integer getAttendanceBlockAfterDays() {
        return attendanceBlockAfterDays;
    }

    public void setAttendanceBlockAfterDays(Integer attendanceBlockAfterDays) {
        this.attendanceBlockAfterDays = attendanceBlockAfterDays;
    }

    public Integer getSeatReleaseAfterDays() {
        return seatReleaseAfterDays;
    }

    public void setSeatReleaseAfterDays(Integer seatReleaseAfterDays) {
        this.seatReleaseAfterDays = seatReleaseAfterDays;
    }

    public Integer getReservationTimeoutMinutes() {
        return reservationTimeoutMinutes;
    }

    public void setReservationTimeoutMinutes(Integer reservationTimeoutMinutes) {
        this.reservationTimeoutMinutes = reservationTimeoutMinutes;
    }

    public Boolean getAllowQrAttendance() {
        return allowQrAttendance;
    }

    public void setAllowQrAttendance(Boolean allowQrAttendance) {
        this.allowQrAttendance = allowQrAttendance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
