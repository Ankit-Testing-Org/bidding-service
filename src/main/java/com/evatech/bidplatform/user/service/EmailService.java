package com.evatech.bidplatform.user.service;

import java.util.List;

public interface EmailService {

    void sendOtp(String email, String otp);
    void sendVerificationEmail(
            String email, String token
    );
    void  sendEmail(Long contractId,
                    String contractName,
                    List<String> emailAddresses);
}

