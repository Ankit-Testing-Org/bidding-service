package com.evatech.bidplatform.bid.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignBidRequest {

    @NotBlank
    private String assignedTo;

    private String comment;
}