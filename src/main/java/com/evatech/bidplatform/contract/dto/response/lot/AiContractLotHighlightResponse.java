package com.evatech.bidplatform.contract.dto.response.lot;

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

    /**
     * STRENGTH
     * WEAKNESS
     * MISSING_REQUIREMENT
     * TECHNICAL_REQUIREMENT
     * COMPLIANCE_REQUIREMENT
     * CAPABILITY_GAP
     * RECOMMENDATION
     */
    private LotHighlightCategory category;

    /**
     * Short title shown in the UI
     */
    private String title;

    /**
     * Detailed explanation from AI
     */
    private String description;

    /**
     * Page where the requirement was found
     */
    private Integer pageNumber;

    /**
     * Clause or section reference
     */
    private String reference;

    /**
     * LOW / MEDIUM / HIGH
     */
    private RiskLevel riskLevel;

    /**
     * 0-100 confidence
     */
    private Double confidenceScore;

    /**
     * Action proposed by AI
     */
    private String recommendedAction;

    /**
     * Whether supplier can satisfy this requirement
     */
    private Boolean bidCapable;
}