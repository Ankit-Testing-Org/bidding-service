package com.evatech.bidplatform.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String identifier; // email or mobile
    private String otp;
    private String newPassword;
}

