package com.evatech.bidplatform.dashboard.dto.report.response;

import java.math.BigDecimal;

public record BidOutcomeResponse(

        String outcome,

        String label,

        BigDecimal value,

        BigDecimal percentage,

        String color

) {
}