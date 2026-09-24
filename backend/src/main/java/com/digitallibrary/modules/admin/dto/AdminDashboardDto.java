package com.digitallibrary.modules.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdminDashboardDto {

    private Long libraryId;
    private int totalSeats;
    private long occupiedSeats;
    private long availableSeats;
    private long reservedSeats;
    private long maintenanceSeats;

    private long todayAttendance;
    private long currentlyInside;

    private long feesDueCount;
    private long overdueCount;
    private long newAdmissionsCount;

    private BigDecimal currentMonthRevenue;
    private BigDecimal pendingRevenue;

    private List<ActionItemDto> actionRequired;

    public AdminDashboardDto() {
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

    public long getOccupiedSeats() {
        return occupiedSeats;
    }

    public void setOccupiedSeats(long occupiedSeats) {
        this.occupiedSeats = occupiedSeats;
    }

    public long getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(long availableSeats) {
        this.availableSeats = availableSeats;
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

    public long getTodayAttendance() {
        return todayAttendance;
    }

    public void setTodayAttendance(long todayAttendance) {
        this.todayAttendance = todayAttendance;
    }

    public long getCurrentlyInside() {
        return currentlyInside;
    }

    public void setCurrentlyInside(long currentlyInside) {
        this.currentlyInside = currentlyInside;
    }

    public long getFeesDueCount() {
        return feesDueCount;
    }

    public void setFeesDueCount(long feesDueCount) {
        this.feesDueCount = feesDueCount;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public long getNewAdmissionsCount() {
        return newAdmissionsCount;
    }

    public void setNewAdmissionsCount(long newAdmissionsCount) {
        this.newAdmissionsCount = newAdmissionsCount;
    }

    public BigDecimal getCurrentMonthRevenue() {
        return currentMonthRevenue;
    }

    public void setCurrentMonthRevenue(BigDecimal currentMonthRevenue) {
        this.currentMonthRevenue = currentMonthRevenue;
    }

    public BigDecimal getPendingRevenue() {
        return pendingRevenue;
    }

    public void setPendingRevenue(BigDecimal pendingRevenue) {
        this.pendingRevenue = pendingRevenue;
    }

    public List<ActionItemDto> getActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(List<ActionItemDto> actionRequired) {
        this.actionRequired = actionRequired;
    }
}
