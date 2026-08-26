package com.evatech.bidplatform.contract.dto.response.lot;

import java.math.BigDecimal;

public record ContractLotResponse(
        Long lotId,
        String lotNumber,
        String lotName,
        BigDecimal valuation,
        String qualificationStatus,
        String analysisStatus,
        String analysisReviewStatus,
        Integer highlightCount
) {
}