package com.evatech.bidplatform.dashboard.dto.contract.response;

public record ContractAnalysisAccessResponse(
        Long contractId,
        boolean assignedToCurrentUser,
        boolean canOpenAnalysis,
        String reason,
        String contractStatus,
        String assignmentStatus,
        String assignedTo
) {
}