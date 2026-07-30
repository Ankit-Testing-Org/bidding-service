package com.evatech.bidplatform.ai.model;

import com.evatech.bidplatform.contract.entity.ContractHighlight;

import java.util.List;

public record AiContractAnalysisResponse(List<ContractHighlight> highlights) {
}