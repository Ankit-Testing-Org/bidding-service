package com.evatech.bidplatform.dashboard.dto.contract.response;

import java.math.BigDecimal;

public record ContractSummaryResponse(
        long totalContracts,
        long assignedToMe,
        long uploaded,
        long underAnalysis,
        long unassigned,
        BigDecimal totalValuation
) {
}