package com.evatech.bidplatform.user.service.impl;

import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import com.evatech.bidplatform.user.service.KeycloakService;
import com.evatech.bidplatform.user.service.OtpService;
import com.evatech.bidplatform.user.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepo;
    private final OtpService otpService;
    private final KeycloakService keycloakService;

    @Override
    public void requestOtp(String identifier) {
        otpService.generateAndSendOtp(identifier);
    }

    @Override
    public void resetPassword(String identifier, String otp, String newPassword) {
        otpService.validateOtp(identifier, otp);

        User user = userRepo.findByEmail(identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        keycloakService.updatePassword(user, newPassword);
    }
}
