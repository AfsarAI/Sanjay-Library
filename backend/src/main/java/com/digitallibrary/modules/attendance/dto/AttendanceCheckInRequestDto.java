package com.digitallibrary.modules.attendance.dto;

import jakarta.validation.constraints.NotBlank;

public class AttendanceCheckInRequestDto {

    @NotBlank(message = "QR Token is required")
    private String qrToken;

    public AttendanceCheckInRequestDto() {
    }

    public AttendanceCheckInRequestDto(String qrToken) {
        this.qrToken = qrToken;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}
