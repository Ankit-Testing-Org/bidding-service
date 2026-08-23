package com.evatech.bidplatform.dashboard.dto.bid.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitBidForApprovalRequest(

        @NotNull(message = "Uploaded bid document ID is required")
        Long uploadedDocumentId,

        Long sourceGeneratedDocumentId,

        @Size(max = 1000, message = "Submission comment cannot exceed 1000 characters")
        String comment
) {
}