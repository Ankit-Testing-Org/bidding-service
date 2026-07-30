package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.ContractPageText;

import java.util.List;

public interface ContractTextExtractionService {
    List<ContractPageText> extractText(Long contractId);
}
