package com.evatech.bidplatform.user.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String addressLine1;
    private String addressLine2;
    private String postCode;
    private String city;
    private String state;
    private String country;
    private String phoneNumber;
}
