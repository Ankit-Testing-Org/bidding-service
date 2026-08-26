package com.evatech.bidplatform.contract.dto.response.contract;

import java.time.LocalDateTime;

public record AssignContractResponse(
        boolean success,
        Long contractId,
        String assignedTo,
        String assignedToName,
        LocalDateTime assignedAt,
        String message,
        String contractName,
        String assignmentStatus
) {
}