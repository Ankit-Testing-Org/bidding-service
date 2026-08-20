package com.evatech.bidplatform.dashboard.dto.report.response;

import java.math.BigDecimal;

public record ReportDashboardResponse(

        BigDecimal pipelineValue,

        BigDecimal totalPortfolioValue,

        BigDecimal winRate,

        Long openRiskCount,

        Long totalContractCount,

        Long totalProposalCount,

        Long pendingReviewCount,

        Long approvalBottleneckCount,

        String currency

) {
}