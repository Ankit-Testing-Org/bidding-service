package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.dto.response.lot.ContractLotAnalysisResultResponse;
import com.evatech.bidplatform.contract.dto.response.lot.ProposalReadinessResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ContractLotService {

    ContractLot qualifyLot(
            Long contractId,
            Long lotId,
            User user,
            List<String> roles);

    ContractLot unqualifyLot(
            Long contractId,
            Long lotId,
            User user,
            List<String> roles);

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
            Long lotId,
            List<String> roles
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

    ContractLot extractLot(Long contractId, User user, Long lotId);

    ProposalReadinessResponse fetchProposalReadiness(
            Long contractId,
            User user,
            List<String> roles
    );
}
