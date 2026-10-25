package com.skipthesyllabus.exception;

public class AIServiceException extends RuntimeException {
    private final String errorCode;

    public AIServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AIServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
