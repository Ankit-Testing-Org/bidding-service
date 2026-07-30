package com.evatech.bidplatform.user.service;


public interface OtpService {

    void generateAndSendOtp(String identifier);

    void validateOtp(String identifier, String otp);
}
