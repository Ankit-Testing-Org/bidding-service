package com.evatech.bidplatform.user.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {

    private String email;
    private String addressLine1;
    private String addressLine2;
    private String postCode;
    private String city;
    private String state;
    private String phoneNumber;
    private String country;
    private String barcodeValue;
}
