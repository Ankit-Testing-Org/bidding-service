package com.evatech.bidplatform.user.service;

public interface EmailService {

    void sendOtp(String email, String otp);
    void sendVerificationEmail(String email, String token);
}
