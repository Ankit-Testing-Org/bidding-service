package com.evatech.bidplatform.contract.dto;

import com.evatech.bidplatform.contract.entity.analysis.ContractAnalysisSummary;
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
public class ContractAnalysisResult {
    private ContractAnalysisSummary summary;
    private List<ContractAnalysisSection> sections;
}

