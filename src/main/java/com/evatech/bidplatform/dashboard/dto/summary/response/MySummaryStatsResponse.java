package com.evatech.bidplatform.dashboard.dto.summary.response;

import java.math.BigDecimal;

public record MySummaryStatsResponse(

        Long openTaskCount,

        Long dueSoonCount,

        BigDecimal pipelineValue,

        String currency,

        Long assignedProposalCount,

        Long pendingReviewCount,

        Long pendingApprovalCount,

        Long highPriorityCount,

        BigDecimal reviewProgressPercentage,

        BigDecimal approvalProgressPercentage,

        BigDecimal proposalProgressPercentage

) {
}