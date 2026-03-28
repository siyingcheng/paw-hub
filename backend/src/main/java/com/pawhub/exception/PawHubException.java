package com.pawhub.exception;

/**
 * Base exception for Paw-Hub application
 */
public class PawHubException extends RuntimeException {

    private final String errorCode;

    public PawHubException(String message) {
        super(message);
        this.errorCode = "PAWHUB_ERROR";
    }

    public PawHubException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PawHubException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PAWHUB_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
