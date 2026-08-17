package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.dto.response.ContractLotAnalysisResultResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ContractLotService {

    ContractLot qualifyLot(
            Long contractId,
            String lotNumber,
            User user,
            List<String> roles);

    ContractLot unqualifyLot(
            Long contractId,
            String lotNumber,
            User user,
            List<String> roles);

    List<ContractLot> getContractLots(
            Long contractId,
            User user, List<String> roles);

    List<ContractLot> getQualifiedLots(
            Long contractId,
            User user);

    List<ContractLot> getUnqualifiedLots(
            Long contractId,
            User user);

    List<ContractLot> processExtractingLots(
            ContractDocument contractDocument,
            User user
    );

    List<ContractLot> extractLots(
            Long contractDocument,
            User user,
            String lotNumber
    );

    ContractLotAnalysisResultResponse analyseContractLot(
            Long contractLotId,
            boolean reanalyse,
            User user,
            List<String> roles,
            String userComment
    );

    ContractLotAnalysisResultResponse reanalyseLot(
            Long lotId,
            String userComment,
            User user,
            List<String> roles) ;

    ContractLot approveAnalysis(Long lotId, User user, String comment);

    ContractLot rejectAnalysis(
            Long lotId,
            String comment,
            User user);
}
