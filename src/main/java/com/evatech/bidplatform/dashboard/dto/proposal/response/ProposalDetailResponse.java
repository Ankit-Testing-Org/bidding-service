package com.evatech.bidplatform.dashboard.dto.proposal.response;

import com.evatech.bidplatform.dashboard.dto.ProposalResponse;

import java.util.List;

public record ProposalDetailResponse(

        ProposalResponse proposal,

        List<ProposalHistoryResponse> history

) {
}