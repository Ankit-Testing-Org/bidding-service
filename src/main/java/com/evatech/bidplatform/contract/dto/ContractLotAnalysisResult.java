package com.evatech.bidplatform.contract.dto;

import com.evatech.bidplatform.contract.entity.analysis.ContractLotAnalysis;
import com.evatech.bidplatform.contract.entity.analysis.ContractLotHighlight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractLotAnalysisResult {

    private ContractLotAnalysis analysis;

    private List<ContractLotHighlight> highlights;
}
