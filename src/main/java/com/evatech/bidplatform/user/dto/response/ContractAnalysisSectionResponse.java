package com.evatech.bidplatform.user.dto.response;


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