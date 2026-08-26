package com.evatech.bidplatform.contract.dto.response.highlight;

import com.evatech.bidplatform.contract.entity.analysis.ContractHighlight;
import com.evatech.bidplatform.contract.dto.response.contract.ContractAnalysisSectionType;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractAnalysisSection {

    private ContractAnalysisSectionType type;

    private String title;

    private String subtitle;

    private List<ContractHighlight> highlights;
}