package com.evatech.bidplatform.contract.dto.request.contract;

public record AssignContractRequest(
        String assignedTo,
        String comment
) {
}