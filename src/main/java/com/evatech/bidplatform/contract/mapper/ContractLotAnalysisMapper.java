package com.evatech.bidplatform.contract.mapper;

import com.evatech.bidplatform.contract.dto.response.ContractLotAnalysisResponse;
import com.evatech.bidplatform.contract.dto.response.ContractLotAnalysisResultResponse;
import com.evatech.bidplatform.contract.dto.response.ContractLotHighlightResponse;
import com.evatech.bidplatform.contract.entity.analysis.ContractLotAnalysis;
import com.evatech.bidplatform.contract.entity.analysis.ContractLotHighlight;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring"
)
public interface ContractLotAnalysisMapper {

    ContractLotAnalysisResponse toAnalysisResponse(
            ContractLotAnalysis analysis
    );

    ContractLotHighlightResponse toHighlightResponse(
            ContractLotHighlight highlight
    );

    List<ContractLotHighlightResponse> toHighlightResponses(
            List<ContractLotHighlight> highlights
    );

    default ContractLotAnalysisResultResponse toResponse(
            Long contractLotId,
            ContractLotAnalysis analysis,
            List<ContractLotHighlight> highlights) {

        return new ContractLotAnalysisResultResponse(
                contractLotId,
                toAnalysisResponse(analysis),
                toHighlightResponses(highlights)
        );
    }
}