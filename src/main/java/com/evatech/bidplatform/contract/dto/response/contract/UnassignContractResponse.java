package com.evatech.bidplatform.contract.dto.response.contract;

public record UnassignContractResponse(
        boolean success,
        Long contractId,
        String message
) {
}