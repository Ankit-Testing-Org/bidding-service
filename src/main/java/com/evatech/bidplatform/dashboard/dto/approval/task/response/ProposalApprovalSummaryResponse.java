package com.evatech.bidplatform.dashboard.dto.approval.task.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProposalApprovalSummaryResponse(

        Long proposalId,

        String proposalNumber,

        BigDecimal proposalValue,

        String currency,

        Integer selectedLots,

        String submittedBy,

        LocalDateTime submittedAt,

        String status

) {
}