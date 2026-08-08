package com.evatech.bidplatform.ai.service;

import com.evatech.bidplatform.contract.dto.ContractLotAnalysisResult;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractLot;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.dto.ContractAnalysisResult;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface AiService {

    ContractAnalysisResult analyseContract(ContractDocument contractDocument,
                                           List<ContractPageText> pages);

    ContractLotAnalysisResult analyseContractLot(
            ContractDocument contractDocument,
            ContractLot contractLot,
            List<ContractPageText> lotPages
    );
}
