package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ContractLifecycleService {

    ContractDocument markAnalysisInProgress(
            Long contractId,
            User user,
            List<String> roles
    );

    ContractDocument markAnalysed(
            Long contractId,
            User user,
            List<String> roles
    );

    void markAnalysisFailed(
            Long contractId,
            String failureReason,
            User user,
            List<String> roles
    );

    ContractDocument updateContractDocumentStatus(
            Long contractDocument,
            ContractDocument contract,
            ContractStatus contractStatus,
            User user, List<String> roles);
}