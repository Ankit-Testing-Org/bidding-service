package com.evatech.bidplatform.dashboard.dto.report.response;

import com.evatech.bidplatform.dashboard.dto.report.ReportPeriod;

import java.math.BigDecimal;
import java.util.List;

public record ReportAnalyticsResponse(

        ReportPeriod period,

        String currency,

        BigDecimal totalPortfolioValue,

        List<PipelineDistributionResponse> pipelineDistribution,

        List<BidOutcomeResponse> outcomeDistribution

) {
}