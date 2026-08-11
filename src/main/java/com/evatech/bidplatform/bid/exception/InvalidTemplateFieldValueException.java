package com.evatech.bidplatform.bid.exception;

public class InvalidTemplateFieldValueException extends RuntimeException {

    public InvalidTemplateFieldValueException(String message) {
        super(message);
    }

    public InvalidTemplateFieldValueException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}