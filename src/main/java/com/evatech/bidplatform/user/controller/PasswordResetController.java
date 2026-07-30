package com.evatech.bidplatform.user.controller;

import com.evatech.bidplatform.user.dto.request.ResetPasswordRequest;
import com.evatech.bidplatform.user.service.PasswordResetService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/password")
@AllArgsConstructor
public class PasswordResetController {

    private final PasswordResetService resetService;


    // STEP 1: Send OTP
    @GetMapping("/send-otp")
    public String sendOtp(@RequestParam String identifier) {
        resetService.requestOtp(identifier);
        return "OTP sent successfully";
    }

    // STEP 2: Verify OTP & Reset
    @PostMapping("/reset")
    public String resetPassword(@RequestBody ResetPasswordRequest req) {
        resetService.resetPassword(
                req.getIdentifier(),
                req.getOtp(),
                req.getNewPassword()
        );
        return "Password reset successful";
    }
}
