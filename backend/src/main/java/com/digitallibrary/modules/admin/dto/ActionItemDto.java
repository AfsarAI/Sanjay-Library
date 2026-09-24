package com.digitallibrary.modules.admin.dto;

public class ActionItemDto {

    private String type; // OVERDUE, EXPIRING_SOON, NEW_ADMISSION, ATTENDANCE_ISSUE
    private String title;
    private String description;
    private Long entityId;
    private String studentName;
    private String studentPhone;
    private String seatNumber;

    public ActionItemDto() {
    }

    public ActionItemDto(String type, String title, String description, Long entityId,
                         String studentName, String studentPhone, String seatNumber) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.entityId = entityId;
        this.studentName = studentName;
        this.studentPhone = studentPhone;
        this.seatNumber = seatNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
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

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
