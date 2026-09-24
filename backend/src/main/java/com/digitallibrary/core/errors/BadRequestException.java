package com.digitallibrary.core.errors;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    public BadRequestException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode != null ? errorCode : "BAD_REQUEST");
    }

    public BadRequestException(String message) {
        this(message, "BAD_REQUEST");
    }
}
