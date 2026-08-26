package com.evatech.bidplatform.contract.dto.response.lot;


import java.util.List;

public record ContractLotAnalysisResultResponse(
        Long contractLotId,
        ContractLotAnalysisResponse analysis,
        List<ContractLotHighlightResponse> highlights
) {
}
