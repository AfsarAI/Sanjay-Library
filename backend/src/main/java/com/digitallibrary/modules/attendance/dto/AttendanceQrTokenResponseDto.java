package com.digitallibrary.modules.attendance.dto;

import java.time.Instant;

public class AttendanceQrTokenResponseDto {

    private String qrToken;
    private Instant expiresAt;
    private long ttlSeconds;

    public AttendanceQrTokenResponseDto() {
    }

    public AttendanceQrTokenResponseDto(String qrToken, Instant expiresAt, long ttlSeconds) {
        this.qrToken = qrToken;
        this.expiresAt = expiresAt;
        this.ttlSeconds = ttlSeconds;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
