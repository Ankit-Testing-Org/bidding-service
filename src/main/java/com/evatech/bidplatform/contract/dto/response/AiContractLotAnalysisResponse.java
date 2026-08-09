package com.evatech.bidplatform.contract.dto.response;

import com.evatech.bidplatform.contract.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiContractLotAnalysisResponse {

    private Boolean recommendedToBid;

    private Double winProbability;

    private RiskLevel overallRiskLevel;

    private String executiveSummary;

    private String recommendation;

    private List<AiContractLotHighlightResponse> highlights;
}