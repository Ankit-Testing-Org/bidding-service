package com.evatech.bidplatform.user.service.impl;


import com.evatech.bidplatform.user.entity.OtpToken;
import com.evatech.bidplatform.user.repository.OtpTokenRepository;
import com.evatech.bidplatform.user.service.EmailService;
import com.evatech.bidplatform.user.service.OtpService;
import com.evatech.bidplatform.user.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpRepo;
    private final OtpGenerator otpGenerator;
    private final EmailService emailService;

    @Override
    public void generateAndSendOtp(String identifier) {
        String otp = otpGenerator.generateOtp();

        OtpToken token = new OtpToken();
        token.setIdentifier(identifier);
        token.setOtp(otp);
        token.setExpiry(LocalDateTime.now().plusMinutes(5));

        otpRepo.save(token);
        sendOtp(identifier, otp);
    }

    private void sendOtp(String identifier, String otp) {
        if (identifier.contains("@")) {
            log.info("EMAIL OTP to  {} : {}",identifier, otp);
            emailService.sendOtp(identifier, otp);
        }
    }

    @Override
    public void validateOtp(String identifier, String otp) {
        OtpToken token = otpRepo
                .findByIdentifierAndOtpAndUsedFalse(identifier, otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (token.getExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        token.setUsed(true);
        otpRepo.save(token);
    }
}
