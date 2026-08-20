package com.evatech.bidplatform.dashboard.dto.review.response;

public record ReviewDashboardResponse(

        Long totalReviewCount,

        Long pendingCount,

        Long approvedCount,

        Long changesRequestedCount,

        Long rejectedCount,

        Long highPriorityCount,

        Long mediumPriorityCount,

        Long lowPriorityCount

) {
}