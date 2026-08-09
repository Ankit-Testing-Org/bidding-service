package com.evatech.bidplatform.contract.dto.response;

import com.evatech.bidplatform.contract.entity.RiskLevel;
import com.evatech.bidplatform.contract.entity.analysis.LotHighlightCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiContractLotHighlightResponse {

    private LotHighlightCategory category;

    private String title;

    private String description;

    private Integer pageNumber;

    private RiskLevel riskLevel;

    private Double confidenceScore;

    private String recommendedAction;

    private Boolean bidCapable;
}