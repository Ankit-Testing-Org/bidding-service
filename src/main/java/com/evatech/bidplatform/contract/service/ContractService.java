package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractHighlight;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContractService {

    ContractDocument uploadContract(MultipartFile file, String uploadedBy);

    public ContractDocument getContract(Long contractId);

    public ContractDocument saveContract(ContractDocument contractDocument);

    List<ContractPageText> getContractPages(Long contractId, Integer pageNumber);

    List<ContractHighlight> getHighlights(Long contractId);

    ContractDocument markAnalysisInProgress(Long contractId);

    ContractDocument markAnalysed(Long contractId);

    List<ContractDocument> getUnassignedContracts();

    List<ContractDocument> getAssignedContracts(String assignedTo);

    ContractDocument assignContract(Long contractId, String assignedTo);

    ContractDocument unassignContract(Long contractId);
}
