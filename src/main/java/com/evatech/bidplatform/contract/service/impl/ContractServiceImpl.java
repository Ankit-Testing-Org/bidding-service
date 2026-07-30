package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.contract.entity.*;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import com.evatech.bidplatform.contract.repository.ContractHighlightRepository;
import com.evatech.bidplatform.contract.repository.ContractPageTextRepository;
import com.evatech.bidplatform.document.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.evatech.bidplatform.contract.service.ContractService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractDocumentRepository contractDocumentRepository;
    private final ContractPageTextRepository contractPageTextRepository;
    private final ContractHighlightRepository contractHighlightRepository;
    private final FileStorageService fileStorageService;

    @Override
    public ContractDocument uploadContract(MultipartFile file, String uploadedBy) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Contract file must not be empty");
        }

        String originalFileName = file.getOriginalFilename();
        String fileType = file.getContentType();

        String storagePath = fileStorageService.storeContract(file);

        ContractDocument contractDocument = ContractDocument.builder()
                .originalFileName(originalFileName)
                .fileType(fileType)
                .storagePath(storagePath)
                .uploadedBy(uploadedBy)
                .uploadedAt(LocalDateTime.now())
                .status(ContractStatus.UPLOADED)
                .assignmentStatus(ContractAssignmentStatus.UNASSIGNED)
                .build();

        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDocument getContract(Long contractId) {
       return getContractOrThrow(contractId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractPageText> getContractPages(
            Long contractId,
            Integer pageNumber
    ) {
        ContractDocument contractDocument = getContractOrThrow(contractId);
        if (pageNumber == null) {
            return contractPageTextRepository
                    .findByContractDocumentIdOrderByPageNumberAsc(contractId);
        }
        ContractPageText pageText = contractPageTextRepository
                .findByContractDocumentIdAndPageNumber(contractId, pageNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Page not found for contract id: "
                                + contractId
                                + " and page number: "
                                + pageNumber
                ));
        return List.of(pageText);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractHighlight> getHighlights(Long contractId) {
        return contractHighlightRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDocument markAnalysisInProgress(Long contractId) {
        ContractDocument contractDocument = getContractOrThrow(contractId);

        if (!ContractStatus.TEXT_EXTRACTED.equals(contractDocument.getStatus())
                && !ContractStatus.ANALYSED.equals(contractDocument.getStatus())) {
            throw new IllegalStateException(
                    "Contract text must be extracted before analysis can start"
            );
        }
        contractDocument.setStatus(ContractStatus.ANALYSIS_IN_PROGRESS);
        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDocument markAnalysed(Long contractId) {
        ContractDocument contractDocument = getContractOrThrow(contractId);

        if (!ContractStatus.ANALYSIS_IN_PROGRESS.equals(contractDocument.getStatus())) {
            throw new IllegalStateException(
                    "Contract must be in analysis progress before marking as analysed"
            );
        }
        contractDocument.setStatus(ContractStatus.ANALYSED);
        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDocument> getUnassignedContracts() {
        return contractDocumentRepository.findByAssignmentStatus(
                ContractAssignmentStatus.UNASSIGNED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDocument> getAssignedContracts(String assignedTo) {
        if (assignedTo == null || assignedTo.isBlank()) {
            throw new IllegalArgumentException("Assigned to must not be empty");
        }

        return contractDocumentRepository.findByAssignedToAndAssignmentStatus(
                assignedTo,
                ContractAssignmentStatus.ASSIGNED
        );
    }

    @Override
    public ContractDocument assignContract(
            Long contractId,
            String assignedTo
    ) {
        if (assignedTo == null || assignedTo.isBlank()) {
            throw new IllegalArgumentException("Assigned to must not be empty");
        }

        ContractDocument contractDocument = getContractOrThrow(contractId);
        contractDocument.assignTo(assignedTo);

        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    public ContractDocument unassignContract(Long contractId) {
        ContractDocument contractDocument = getContractOrThrow(contractId);
        contractDocument.unassign();

        return contractDocumentRepository.save(contractDocument);
    }

    private ContractDocument getContractOrThrow(Long contractId) {
        return contractDocumentRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contract not found with id: " + contractId
                ));
    }
}