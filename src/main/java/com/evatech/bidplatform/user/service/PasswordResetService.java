package com.evatech.bidplatform.user.service;

public interface PasswordResetService {

    void requestOtp(String identifier);
    void resetPassword(String identifier, String otp, String newPassword);
}
