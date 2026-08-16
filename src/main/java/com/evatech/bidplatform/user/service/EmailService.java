package com.evatech.bidplatform.user.service;

import java.util.List;

public interface EmailService {

    void sendOtp(String email, String otp);
    void sendVerificationEmail(
            String email, String token
    );
    void  sendEmail(List<String> emailAddresses,
                    String subject,
                    String textMessage);
}

