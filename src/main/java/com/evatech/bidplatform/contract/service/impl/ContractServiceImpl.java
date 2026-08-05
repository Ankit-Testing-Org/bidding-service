package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.contract.entity.*;
import com.evatech.bidplatform.contract.repository.ContractAssignmentHistoryRepository;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import com.evatech.bidplatform.contract.repository.ContractHighlightRepository;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.contract.service.ContractTextExtractionService;
import com.evatech.bidplatform.contract.service.LotExtractionService;
import com.evatech.bidplatform.document.service.FileStorageService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractDocumentRepository contractDocumentRepository;
    private final ContractHighlightRepository contractHighlightRepository;
    private final ContractAssignmentHistoryRepository contractAssignmentHistoryRepository;
    private final FileStorageService fileStorageService;
    private final LotExtractionService lotExtractionService;
    private final ContractTextExtractionService contractTextExtractionService;

    @Override
    public ContractDocument uploadContract(
            MultipartFile file,
            User user) {

        // STEP 1) VALIDATE FILE
        validateFile(file, user.getEmail());

        // STEP 2) STORE FILE
        String storagePath = fileStorageService.storeContract(file);

        // STEP 3) UPDATE CONTRACT DOCUMENT DB
        ContractDocument contractDocument = ContractDocument.builder()
                        .originalFileName(file.getOriginalFilename())
                        .fileType(file.getContentType())
                        .storagePath(storagePath)
                        .uploadedBy(user.getEmail())
                        .status(ContractStatus.UPLOADED)
                        .assignmentStatus(
                                ContractAssignmentStatus.UNASSIGNED
                        )
                        .build();

        contractDocument = contractDocumentRepository.save(contractDocument);

        // STEP 4) Extract text and save pages
        contractTextExtractionService.extractText(contractDocument.getId(), user);

        // STEP 5) Extract lots and save them
        lotExtractionService.extractLots(contractDocument, user);

        return contractDocumentRepository.findById(contractDocument.getId()).orElse(null);
    }


    @Override
    @Transactional(readOnly = true)
    public ContractDocument getContract(Long contractId, User user) {
        validateUser(user);
        return getContractOrThrow(contractId);
    }

    @Override
    public ContractDocument saveContract(ContractDocument contractDocument) {
        if (contractDocument == null) {
            throw new IllegalArgumentException("Contract document must not be null");
        }

        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractPageText> getContractPages(
            Long contractId,
            Integer pageNumber,
            User user) {

        ContractDocument contractDocument = getContract(contractId, user);
        return contractTextExtractionService.getContractPages(contractDocument, pageNumber, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractLot> getContractLots(
            Long contractId,
            User user,
            String lotNumber) {

        ContractDocument contractDocument = getContract(contractId, user);
        return lotExtractionService.extractLots(contractDocument, user, lotNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractHighlight> getHighlights(Long contractId, User user) {
        validateUser(user);
        validateContractId(contractId);
        return contractHighlightRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId);
    }

    @Override
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
    public List<ContractDocument> getUnassignedContracts(User user) {
        return contractDocumentRepository.findByAssignmentStatus(
                ContractAssignmentStatus.UNASSIGNED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDocument> getAssignedContracts(String assignedTo, User user) {
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
            String assignedTo,
            User user) {

        String assignedBy = getUserEmail(user);

        if (assignedTo == null || assignedTo.isBlank()) {
            assignedTo = assignedBy;
        }
        ContractDocument contractDocument = getContractOrThrow(contractId);

        String oldAssignee = contractDocument.getAssignedTo();

        contractDocument.assignTo(
                assignedTo,
                assignedBy
        );

        String remarks = resolveAssignmentRemarks(
                oldAssignee,
                assignedTo
        );

        createAssignmentHistory(
                contractDocument,
                oldAssignee,
                assignedTo,
                assignedBy,
                remarks
        );

        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    public ContractDocument reassignContract(
            Long contractId,
            String newAssignee,
            User user) {

        if (newAssignee == null || newAssignee.isBlank()) {
            throw new IllegalArgumentException("New assignee must not be empty");
        }

        String assignedBy = getUserEmail(user);

        ContractDocument contractDocument = getContractOrThrow(contractId);

        String oldAssignee = contractDocument.getAssignedTo();

        if (oldAssignee == null || oldAssignee.isBlank()) {
            throw new IllegalStateException(
                    "Contract is not currently assigned. Please assign it first."
            );
        }

        contractDocument.reassignTo(
                newAssignee,
                assignedBy
        );

        String remarks = oldAssignee.equals(newAssignee)
                ? "Assignment updated"
                : "Contract reassigned";

        createAssignmentHistory(
                contractDocument,
                oldAssignee,
                newAssignee,
                assignedBy,
                remarks
        );

        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    public ContractDocument unassignContract(Long contractId, User user) {
        String assignedBy = getUserEmail(user);

        ContractDocument contractDocument = getContractOrThrow(contractId);

        String oldAssignee = contractDocument.getAssignedTo();

        if (oldAssignee == null || oldAssignee.isBlank()) {
            throw new IllegalStateException("Contract is already unassigned");
        }

        contractDocument.unassign(assignedBy);

        createAssignmentHistory(
                contractDocument,
                oldAssignee,
                null,
                assignedBy,
                "Contract unassigned"
        );

        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractAssignmentHistory> getAssignmentHistory(Long contractId, User user) {
        validateUser(user);
        validateContractId(contractId);

        return contractAssignmentHistoryRepository
                .findByContractDocumentIdOrderByAssignedAtDesc(contractId);
    }

    private void createAssignmentHistory(
            ContractDocument contractDocument,
            String oldAssignee,
            String newAssignee,
            String assignedBy,
            String remarks) {

        ContractAssignmentHistory history = ContractAssignmentHistory.builder()
                .contractDocument(contractDocument)
                .oldAssignee(oldAssignee)
                .newAssignee(newAssignee)
                .assignedBy(assignedBy)
                .assignedAt(LocalDateTime.now())
                .remarks(remarks)
                .build();

        contractAssignmentHistoryRepository.save(history);
    }

    private String resolveAssignmentRemarks(
            String oldAssignee,
            String newAssignee) {

        if (oldAssignee == null || oldAssignee.isBlank()) {
            return "Initial assignment";
        }

        if (oldAssignee.equals(newAssignee)) {
            return "Assignment updated";
        }

        return "Contract reassigned";
    }

    private ContractDocument getContractOrThrow(Long contractId) {
        validateContractId(contractId);

        return contractDocumentRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contract not found with id: " + contractId
                ));
    }

    private void validateContractId(Long contractId) {
        if (contractId == null) {
            throw new IllegalArgumentException("Contract id must not be null");
        }
    }

    private String validateUser(User user) {
        return getUserEmail(user);
    }

    private String getUserEmail(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("User email must not be empty");
        }

        return user.getEmail();
    }

    private void validateFile(MultipartFile file, String uploadedBy) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Contract file must not be empty");
        }

        if (uploadedBy == null || uploadedBy.isBlank()) {
            throw new IllegalArgumentException("Uploaded by must not be empty");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Original file name must not be empty");
        }

        String fileType = file.getContentType();
        if (fileType == null || fileType.isBlank()) {
            throw new IllegalArgumentException("File type must not be empty");
        }
    }
}