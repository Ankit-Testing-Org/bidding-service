package com.evatech.bidplatform.contract.dto.response;

import com.evatech.bidplatform.contract.entity.RiskLevel;
import com.evatech.bidplatform.contract.entity.analysis.LotHighlightCategory;

public record ContractLotHighlightResponse(

        Long id,

        LotHighlightCategory category,

        String title,

        String description,

        Integer pageNumber,

        String reference,

        RiskLevel riskLevel,

        Double confidenceScore,

        String recommendedAction,

        Boolean bidCapable

) {
}