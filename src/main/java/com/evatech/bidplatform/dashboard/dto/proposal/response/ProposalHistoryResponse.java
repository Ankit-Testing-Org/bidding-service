package com.evatech.bidplatform.dashboard.dto.proposal.response;

import java.time.LocalDateTime;

public record ProposalHistoryResponse(

        Long id,

        String action,

        String comment,

        String performedBy,

        LocalDateTime performedAt

) {
}
