package com.evatech.bidplatform.contract.dto.response.lot;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ExtractedLotResponse {

    private String lotNumber;

    private String lotName;

    private String description;

    private Integer startPage;

    private Integer endPage;

    private BigDecimal valuation;
}
