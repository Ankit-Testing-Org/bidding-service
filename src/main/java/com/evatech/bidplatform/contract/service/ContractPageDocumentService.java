package com.evatech.bidplatform.contract.service;

import org.springframework.core.io.Resource;

public interface ContractPageDocumentService {

    Resource getContractPageAsDocument(
            Long contractId,
            Integer pageNumber
    );

    String getGeneratedFileName(
            Long contractId,
            Integer pageNumber
    );
}
