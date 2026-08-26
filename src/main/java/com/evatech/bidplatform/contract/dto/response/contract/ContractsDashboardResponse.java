package com.evatech.bidplatform.contract.dto.response.contract;


import java.util.List;

public record ContractsDashboardResponse(
        ContractsSummariesResponse summary,
        List<ContractListItemResponse> contracts
) {
}