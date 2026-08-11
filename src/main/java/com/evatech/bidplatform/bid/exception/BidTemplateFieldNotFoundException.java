package com.evatech.bidplatform.bid.exception;

public class BidTemplateFieldNotFoundException extends RuntimeException {

    public BidTemplateFieldNotFoundException(String message) {
        super(message);
    }

    public BidTemplateFieldNotFoundException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}