package com.evatech.bidplatform.dashboard.dto.proposal.request;

import com.evatech.bidplatform.dashboard.entity.ProposalStatus;

public record ProposalSearchRequest(

        String searchText,

        ProposalStatus status,

        Integer page,

        Integer size,

        String sortBy,

        String sortDirection

) {
}