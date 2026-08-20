package com.evatech.bidplatform.dashboard.dto.proposal.response;

import java.math.BigDecimal;

public record ProposalDashboardResponse(

        Long totalProposalCount,

        Long draftCount,

        Long activeCount,

        Long reviewCount,

        Long submittedCount,

        Long wonCount,

        Long lostCount,

        Long withdrawnCount,

        Long notBiddedCount,

        BigDecimal activeProposalValue,

        BigDecimal submittedProposalValue,

        BigDecimal wonProposalValue

) {
}