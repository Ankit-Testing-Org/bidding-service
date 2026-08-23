package com.evatech.bidplatform.dashboard.dto.bid.response;

import java.time.LocalDateTime;

public record SubmitBidForApprovalResponse(

        Long contractId,

        Long bidId,

        Long uploadedDocumentId,

        Long workflowInstanceId,

        String bidStatus,

        String workflowStatus,

        LocalDateTime submittedAt,

        String submittedBy,

        String message
) {
}