package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.dto.request.contract.ContractSearchRequest;
import com.evatech.bidplatform.contract.entity.*;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.dashboard.dto.ProcessingQueueItemResponse;
import com.evatech.bidplatform.contract.dto.response.highlight.ContractHighlightResponse;
import com.evatech.bidplatform.user.entity.User;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContractService {

    ContractDocument uploadContract(MultipartFile file, User uploadedBy, List<String> roles);

    public ContractDocument getContract(Long contractId, User user, List<String> roles);

    public ContractDocument saveContract(ContractDocument contractDocument);

    List<ContractPageText> getContractPages(Long contractId, Integer pageNumber, User user, List<String> roles);

    List<ContractHighlightResponse> getHighlights(Long contractId, User user, List<String> roles);

    List<ContractDocument> getUnassignedContracts(User user, List<String> roles);

    List<ContractDocument> getAssignedContracts(String assignedTo, User user, List<String> roles);

    ContractDocument assignContract(
            Long contractId,
            String assignedTo,
            User user,
            List<String> roles,
            String userComment);

    ContractDocument reassignContract(
            Long contractId,
            String newAssignee,
            User user,
            List<String> roles,
            String userComment
    );

    ContractDocument unassignContract(
            Long contractId,
            User user,
            List<String> roles,
            String userComment);

    List<ContractAssignmentHistory> getAssignmentHistory(
            Long contractId,
            User user
    );

    List<ContractLot>  getContractLots(
            Long contractId,
            User user,
            Long lotId,
            List<String> roles);

    List<ContractDocument> getContractDashboard();

    Page<ContractDocument> searchContracts(User user,
                                           List<String> roles,
                                           ContractSearchRequest request);

    Resource retrieveContractPdf(Long contractId,
                                 User user,
                                 List<String> roles);

    List<ProcessingQueueItemResponse> getProcessingQueue(User user, List<String> roles);
}
