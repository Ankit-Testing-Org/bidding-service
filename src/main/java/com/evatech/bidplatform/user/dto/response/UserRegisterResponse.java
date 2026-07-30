package com.evatech.bidplatform.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegisterResponse {
    private String userId;
    private String email;
    private String message;
}
