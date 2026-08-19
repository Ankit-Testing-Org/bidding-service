package com.evatech.bidplatform.dashboard.dto.contract.response;

public record UnassignContractResponse(
        boolean success,
        Long contractId,
        String message
) {
}