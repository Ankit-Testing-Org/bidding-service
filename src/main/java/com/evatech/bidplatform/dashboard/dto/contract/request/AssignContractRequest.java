package com.evatech.bidplatform.dashboard.dto.contract.request;

public record AssignContractRequest(
        String assignedTo,
        String comment
) {
}