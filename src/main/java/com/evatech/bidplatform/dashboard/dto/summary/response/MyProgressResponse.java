package com.evatech.bidplatform.dashboard.dto.summary.response;

import java.math.BigDecimal;

public record MyProgressResponse(

        BigDecimal reviewProgressPercentage,

        BigDecimal approvalProgressPercentage,

        BigDecimal proposalProgressPercentage,

        Long completedReviewCount,

        Long totalReviewCount,

        Long completedApprovalCount,

        Long totalApprovalCount,

        Long completedProposalCount,

        Long totalProposalCount

) {
}
