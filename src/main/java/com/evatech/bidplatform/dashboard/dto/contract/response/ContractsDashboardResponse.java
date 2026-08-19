package com.evatech.bidplatform.dashboard.dto.contract.response;


import java.util.List;

public record ContractsDashboardResponse(
        ContractSummaryResponse summary,
        List<ContractListItemResponse> contracts
) {
}