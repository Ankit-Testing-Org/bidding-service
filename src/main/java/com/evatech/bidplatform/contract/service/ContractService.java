package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.ContractAssignmentHistory;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractHighlight;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContractService {

    ContractDocument uploadContract(MultipartFile file, String uploadedBy);

    public ContractDocument getContract(Long contractId, User user);

    public ContractDocument saveContract(ContractDocument contractDocument);

    List<ContractPageText> getContractPages(Long contractId, Integer pageNumber, User user);

    List<ContractHighlight> getHighlights(Long contractId, User user);

    ContractDocument markAnalysisInProgress(Long contractId);

    ContractDocument markAnalysed(Long contractId);

    List<ContractDocument> getUnassignedContracts(User user);

    List<ContractDocument> getAssignedContracts(String assignedTo, User user);

    ContractDocument assignContract(
            Long contractId,
            String assignedTo,
            User user
    );

    ContractDocument reassignContract(
            Long contractId,
            String newAssignee,
            User user
    );

    ContractDocument unassignContract(
            Long contractId,
            User user
    );

    List<ContractAssignmentHistory> getAssignmentHistory(
            Long contractId,
            User user
    );
}
