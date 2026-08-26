package com.evatech.bidplatform.contract.dto.response.contract;


import com.evatech.bidplatform.contract.dto.response.highlight.ContractHighlightResponse;

import java.util.List;

public record ContractAnalysisSectionResponse(
        ContractAnalysisSectionType type,
        String title,
        String subtitle,
        Integer itemCount,
        String badgeLabel,
        List<ContractHighlightResponse> highlights
) {
}