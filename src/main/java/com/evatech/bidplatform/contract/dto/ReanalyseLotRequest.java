package com.evatech.bidplatform.contract.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReanalyseLotRequest {

    @NotBlank
    private String userComment;
}