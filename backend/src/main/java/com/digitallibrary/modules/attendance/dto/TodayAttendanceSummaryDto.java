package com.digitallibrary.modules.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public class TodayAttendanceSummaryDto {

    private Long libraryId;
    private LocalDate date;
    private long totalStudents;
    private long checkedInToday;
    private long currentlyInside;
    private long checkedOut;
    private long absent;
    private List<AttendanceRecordDto> recentRecords;

    public TodayAttendanceSummaryDto() {
    }

    public TodayAttendanceSummaryDto(Long libraryId, LocalDate date, long totalStudents,
                                     long checkedInToday, long currentlyInside, long checkedOut,
                                     long absent, List<AttendanceRecordDto> recentRecords) {
        this.libraryId = libraryId;
        this.date = date;
        this.totalStudents = totalStudents;
        this.checkedInToday = checkedInToday;
        this.currentlyInside = currentlyInside;
        this.checkedOut = checkedOut;
        this.absent = absent;
        this.recentRecords = recentRecords;
    }

    public Long getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Long libraryId) {
        this.libraryId = libraryId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getCheckedInToday() {
        return checkedInToday;
    }

    public void setCheckedInToday(long checkedInToday) {
        this.checkedInToday = checkedInToday;
    }

    public long getCurrentlyInside() {
        return currentlyInside;
    }

    public void setCurrentlyInside(long currentlyInside) {
        this.currentlyInside = currentlyInside;
    }

    public long getCheckedOut() {
        return checkedOut;
    }

    public void setCheckedOut(long checkedOut) {
        this.checkedOut = checkedOut;
    }

    public long getAbsent() {
        return absent;
    }

    public void setAbsent(long absent) {
        this.absent = absent;
    }

    public List<AttendanceRecordDto> getRecentRecords() {
        return recentRecords;
    }

    public void setRecentRecords(List<AttendanceRecordDto> recentRecords) {
        this.recentRecords = recentRecords;
    }
}
