package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ContractAccessService {

    ContractDocument getAccessibleContract(
            Long contractId,
            User user,
            List<String> roles
    );

    void validateAccess(
            ContractDocument contractDocument,
            User user,
            List<String> roles
    );


}