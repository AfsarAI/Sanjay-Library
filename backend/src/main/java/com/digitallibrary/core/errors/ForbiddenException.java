package com.digitallibrary.core.errors;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message, String errorCode) {
        super(message, HttpStatus.FORBIDDEN, errorCode != null ? errorCode : "ACCESS_DENIED");
    }

    public ForbiddenException(String message) {
        this(message, "ACCESS_DENIED");
    }
}
