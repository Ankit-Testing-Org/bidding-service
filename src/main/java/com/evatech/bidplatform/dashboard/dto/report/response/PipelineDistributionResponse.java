package com.evatech.bidplatform.dashboard.dto.report.response;

import java.math.BigDecimal;

public record PipelineDistributionResponse(

        String status,

        String label,

        BigDecimal value,

        BigDecimal percentage

) {
}