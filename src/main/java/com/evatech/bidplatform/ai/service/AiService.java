package com.evatech.bidplatform.ai.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractHighlight;
import com.evatech.bidplatform.contract.entity.ContractPageText;

import java.util.List;

public interface AiService {

    List<ContractHighlight> analyseContract(ContractDocument contractDocument,
                                            List<ContractPageText> pages);
}
