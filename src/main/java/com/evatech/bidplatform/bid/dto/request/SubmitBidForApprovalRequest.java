package com.evatech.bidplatform.bid.dto.request;

import com.evatech.bidplatform.user.dto.RoleType;

import java.util.List;

public record SubmitBidForApprovalRequest(

        Long bidId,

        String comment,

        List<RoleType> reviewers
) {
}