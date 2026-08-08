package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ContractTextExtractionService {
    List<ContractPageText> extractText(Long contractId, User user, List<String> roles);

    List<ContractPageText> getContractPages(ContractDocument contractId, Integer pageNumber, User user);
}
