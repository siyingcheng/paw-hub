package com.pawhub.common.exception;

import org.springframework.http.HttpStatus;

public class PawHubException extends RuntimeException {
    private final HttpStatus status;
    public PawHubException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() { return status; }
}
