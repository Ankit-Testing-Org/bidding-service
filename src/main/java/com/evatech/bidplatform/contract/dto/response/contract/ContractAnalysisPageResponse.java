package com.evatech.bidplatform.contract.dto.response.contract;

import com.evatech.bidplatform.contract.dto.response.highlight.ContractHighlightResponse;

import java.util.List;

public record ContractAnalysisPageResponse(
        Long contractDocumentId,
        ContractSummaryResponse summary,
        List<ContractHighlightResponse> highlights
) {
}
