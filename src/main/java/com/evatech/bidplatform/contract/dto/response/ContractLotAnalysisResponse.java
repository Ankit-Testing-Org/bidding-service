package com.evatech.bidplatform.contract.dto.response;


import com.evatech.bidplatform.contract.entity.RiskLevel;

public record ContractLotAnalysisResponse(

        Long id,

        Boolean recommendedToBid,

        Double bidScore,

        Double winProbability,

        RiskLevel overallRiskLevel,

        String executiveSummary,

        String recommendation

) {
}