package com.digitallibrary.core.errors;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
    public ConflictException(String message, String errorCode) {
        super(message, HttpStatus.CONFLICT, errorCode != null ? errorCode : "RESOURCE_CONFLICT");
    }

    public ConflictException(String message) {
        this(message, "RESOURCE_CONFLICT");
    }
}
