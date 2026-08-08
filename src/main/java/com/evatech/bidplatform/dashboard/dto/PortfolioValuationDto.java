package com.evatech.bidplatform.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PortfolioValuationDto {

    private BigDecimal activeValue;

    private BigDecimal wonValue;

    private BigDecimal lostValue;

    private BigDecimal notBiddedValue;
}