package com.evatech.bidplatform.contract.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtractedLotResponse {

    private String lotNumber;

    private String lotName;

    private String description;

    private Integer startPage;

    private Integer endPage;
}
