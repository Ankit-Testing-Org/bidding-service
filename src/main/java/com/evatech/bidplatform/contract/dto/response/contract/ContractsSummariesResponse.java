package com.evatech.bidplatform.contract.dto.response.contract;

import java.math.BigDecimal;

public record ContractsSummariesResponse(
        long totalContracts,
        long assignedToMe,
        long uploaded,
        long underAnalysis,
        long unassigned,
        BigDecimal totalValuation
) {
}