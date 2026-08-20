package com.evatech.bidplatform.dashboard.dto.approval.response;

import java.math.BigDecimal;

public record ApprovalDashboardResponse(

        Long totalApprovalCount,

        Long pendingCount,

        Long approvedCount,

        Long rejectedCount,

        Long delegatedCount,

        Long clarificationRequestedCount,

        BigDecimal approvedValue,

        String currency

) {
}