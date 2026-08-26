package com.evatech.bidplatform.contract.dto.response.contract;

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