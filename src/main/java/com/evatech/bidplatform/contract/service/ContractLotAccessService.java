package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ContractLotAccessService {

    List<ContractLot> getContractLots(
            Long contractId,
            User user,
            List<String> roles
    );
}
