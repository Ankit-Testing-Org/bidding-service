package com.evatech.bidplatform.dashboard.dto.bid.request;

import com.evatech.bidplatform.dashboard.dto.bid.BidDocumentOutputFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GenerateBidDocumentRequest(

        @NotNull(message = "Output format is required")
        BidDocumentOutputFormat outputFormat,

        @Size(max = 2000, message = "Generation notes cannot exceed 2000 characters")
        String generationNotes,

        boolean includeQualifiedLotsOnly,

        boolean includeContractSummary,

        boolean includeComplianceRequirements,

        boolean includeCommercialOffer,

        boolean includeMandatoryDocuments
) {
}