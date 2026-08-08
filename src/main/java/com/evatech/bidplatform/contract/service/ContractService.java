package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.*;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlight;
import com.evatech.bidplatform.user.dto.RoleType;
import com.evatech.bidplatform.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface ContractService {

    ContractDocument uploadContract(MultipartFile file, User uploadedBy, List<String> roles);

    public ContractDocument getContract(Long contractId, User user, List<String> roles);

    public ContractDocument saveContract(ContractDocument contractDocument);

    List<ContractPageText> getContractPages(Long contractId, Integer pageNumber, User user, List<String> roles);

    List<ContractHighlight> getHighlights(Long contractId, User user, List<String> roles);

    ContractDocument markAnalysisInProgress(Long contractId, User user, List<String> roles);

    ContractDocument markAnalysed(Long contractId, User user, List<String> roles);

    List<ContractDocument> getUnassignedContracts(User user, List<String> roles);

    List<ContractDocument> getAssignedContracts(String assignedTo, User user, List<String> roles);

    ContractDocument assignContract(
            Long contractId,
            String assignedTo,
            User user,
            List<String> roles);

    ContractDocument reassignContract(
            Long contractId,
            String newAssignee,
            User user,
            List<String> roles
    );

    ContractDocument unassignContract(
            Long contractId,
            User user,
            List<String> roles);

    List<ContractAssignmentHistory> getAssignmentHistory(
            Long contractId,
            User user
    );

    List<ContractLot>  getContractLots(
            Long contractId,
            User user,
            String lotNumber, List<String> roles);

    void markAnalysisFailed(Long contractId,
                            String failureReason,
                            User user,
                            List<String> roles);
}
