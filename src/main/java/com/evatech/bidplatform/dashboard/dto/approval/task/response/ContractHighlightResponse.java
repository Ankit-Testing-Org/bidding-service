package com.evatech.bidplatform.dashboard.dto.approval.task.response;

public record ContractHighlightResponse(

        Long highlightId,

        String title,

        String description,

        String riskLevel,

        String reference,

        String category

) {
}