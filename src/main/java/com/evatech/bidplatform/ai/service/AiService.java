package com.evatech.bidplatform.ai.service;

import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.contract.dto.response.lot.ContractLotAnalysisResult;
import com.evatech.bidplatform.contract.dto.response.highlight.HighlightReanalysisResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlight;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.dto.response.highlight.ContractAnalysisResult;

import java.util.List;

public interface AiService {

    ContractAnalysisResult analyseContract(ContractDocument contractDocument,
                                           List<ContractPageText> pages);

    ContractLotAnalysisResult analyseContractLot(
            ContractDocument contractDocument,
            ContractLot contractLot,
            List<ContractPageText> lotPages,
            String userComment);

    HighlightReanalysisResponse reanalyseHighlight(ContractDocument contract,
                                                   ContractHighlight highlight,
                                                   String userComment);

    AiBidTemplateResponse generateBidTemplate(String prompt);
}
