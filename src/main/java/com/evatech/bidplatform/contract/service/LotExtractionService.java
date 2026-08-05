package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractLot;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface LotExtractionService {

    List<ContractLot> extractLots(
            ContractDocument contractDocument,
            User user
    );

    List<ContractLot> extractLots(
            ContractDocument contractDocument,
            User user,
            String lotNumber
    );
}