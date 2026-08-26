package com.evatech.bidplatform.user.mapper;

import com.evatech.bidplatform.contract.entity.analysis.ContractAnalysisSummary;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlight;
import com.evatech.bidplatform.contract.dto.response.contract.ContractAnalysisPageResponse;
import com.evatech.bidplatform.contract.dto.response.highlight.ContractHighlightResponse;
import com.evatech.bidplatform.contract.dto.response.contract.ContractSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring"
)
public interface ContractAnalysisMapper {

    ContractSummaryResponse toSummaryResponse(
            ContractAnalysisSummary summary
    );

    ContractHighlightResponse toHighlightResponse(
            ContractHighlight highlight
    );

    List<ContractHighlightResponse> toHighlightResponses(
            List<ContractHighlight> highlights
    );

    @Mapping(
            target = "contractDocumentId",
            source = "contractDocumentId"
    )
    @Mapping(
            target = "summary",
            source = "summary"
    )
    @Mapping(
            target = "highlights",
            source = "highlights"
    )
    ContractAnalysisPageResponse toPageResponse(
            Long contractDocumentId,
            ContractAnalysisSummary summary,
            List<ContractHighlight> highlights
    );
}
