package com.evatech.bidplatform.user.dto.response;

import java.util.List;

public record ContractAnalysisPageResponse(
        Long contractDocumentId,
        ContractSummaryResponse summary,
        List<ContractHighlightResponse> highlights
) {
}
