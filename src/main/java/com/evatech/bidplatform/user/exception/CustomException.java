package com.evatech.bidplatform.user.exception;

public class CustomException extends RuntimeException {

    private final int statusCode;

    // Constructor with message only (default status code 400)
    public CustomException(String message) {
        super(message);
        this.statusCode = 400; // default bad request
    }

    // Constructor with message + custom HTTP status code
    public CustomException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    // Getter for status code
    public int getStatusCode() {
        return statusCode;
    }
}