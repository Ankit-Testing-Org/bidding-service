package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.bid.entity.BidTemplateField;
import com.evatech.bidplatform.bid.exception.BidTemplateFieldNotFoundException;
import com.evatech.bidplatform.bid.repository.BidTemplateFieldRepository;
import com.evatech.bidplatform.bid.service.FileStorageService;
import com.evatech.bidplatform.contract.entity.*;
import com.evatech.bidplatform.contract.entity.analysis.AnalysisStatus;
import com.evatech.bidplatform.contract.entity.analysis.ContractAnalysisSummary;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.repository.*;
import com.evatech.bidplatform.contract.service.ContractHighlightService;
import com.evatech.bidplatform.contract.service.ContractLotService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.contract.service.ContractTextExtractionService;
import com.evatech.bidplatform.user.dto.RoleType;
import com.evatech.bidplatform.user.dto.response.ContractHighlightResponse;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractDocumentRepository contractDocumentRepository;
    private final ContractHighlightRepository contractHighlightRepository;
    private final ContractAssignmentHistoryRepository contractAssignmentHistoryRepository;
    private final FileStorageService fileStorageService;
    private final ContractLotService contractLotService;
    private final ContractTextExtractionService contractTextExtractionService;
    private final ContractAnalysisSummaryRepository contractAnalysisSummaryRepository;
    private final ContractDocumentFieldValueRepository contractDocumentFieldValueRepository;
    private final BidTemplateFieldRepository bidTemplateFieldRepository;
    private final ContractHighlightService contractHighlightService;
    private final EmailService emailService;

    @Override
    public ContractDocument uploadContract(
            MultipartFile file,
            User user, List<String> roles) {

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

        // STEP 4) Extract text and save pages //TODO : NOT NEEDED.
        contractTextExtractionService.
                extractText(contractDocument.getId(), user, roles);

        // STEP 5) Extract lots and save them
        contractLotService.
                processExtractingLots(contractDocument, user);

        // STEP 6) Analyse Contract
        contractHighlightService.analyseContractHighlights(contractDocument.getId(),
                        false, user, roles);

        // STEP 7) TODO : SEND AN EMAIL TO BIDDERS.
        emailService.sendEmail(contractDocument.getId(),
                contractDocument.getOriginalFileName(),
                new ArrayList<>());

        // STEP 8)
        return contractDocumentRepository.findById(contractDocument.getId()).orElse(null);
    }


    @Override
    @Transactional(readOnly = true)
    public ContractDocument getContract(Long contractId, User user, List<String> roles) {
        validateUser(user);
        return getContractOrThrow(contractId, user, roles);
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
            User user, List<String> roles) {

        ContractDocument contractDocument = getContract(contractId, user, roles);
        return contractTextExtractionService.getContractPages(contractDocument, pageNumber, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractLot> getContractLots(
            Long contractId,
            User user,
            String lotNumber, List<String> roles) {
        return contractLotService.extractLots(contractId, user, lotNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractHighlightResponse> getHighlights(Long contractId, User user, List<String> roles) {
        validateUser(user);
        validateContractId(contractId);
        return contractHighlightService.getHighlights(contractId);
    }

    @Override
    public ContractDocument markAnalysisInProgress(Long contractId, User user, List<String> roles) {
        ContractDocument contractDocument = getContractOrThrow(contractId, user, roles);
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
    public ContractDocument markAnalysed(Long contractId, User user, List<String> roles) {
        ContractDocument contractDocument = getContractOrThrow(contractId, user, roles);
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
    public List<ContractDocument> getUnassignedContracts(User user, List<String> roles) {
        return contractDocumentRepository.findByAssignmentStatus(
                ContractAssignmentStatus.UNASSIGNED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDocument> getAssignedContracts(String assignedTo, User user, List<String> roles) {
        if (assignedTo == null || assignedTo.isBlank()) {
            throw new IllegalArgumentException("Assigned to must not be empty");
        }
        return contractDocumentRepository.findByAssignedToAndAssignmentStatus(assignedTo, ContractAssignmentStatus.ASSIGNED);
    }

    @Override
    public ContractDocument assignContract(
            Long contractId, String assignedTo, User user, List<String> roles) {

        String assignedBy = getUserEmail(user);
        if (assignedTo == null || assignedTo.isBlank()) {
            assignedTo = assignedBy;
        }
        ContractDocument contractDocument = getContractOrThrow(contractId, user, roles);
        String oldAssignee = contractDocument.getAssignedTo();
        contractDocument.assignTo(assignedTo, assignedBy);
        String remarks = resolveAssignmentRemarks(oldAssignee, assignedTo);
        createAssignmentHistory(contractDocument, oldAssignee,  assignedTo,  assignedBy, remarks);
        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    public ContractDocument reassignContract(
            Long contractId,
            String newAssignee,
            User user,
            List<String> roles) {

        if (newAssignee == null || newAssignee.isBlank()) {
            throw new IllegalArgumentException("New assignee must not be empty");
        }
        String assignedBy = getUserEmail(user);
        ContractDocument contractDocument = getContractOrThrow(contractId, user, roles);
        String oldAssignee = contractDocument.getAssignedTo();
        if (oldAssignee == null || oldAssignee.isBlank()) {
            throw new IllegalStateException(
                    "Contract is not currently assigned. Please assign it first."
            );
        }
        contractDocument.reassignTo(newAssignee, assignedBy);
        String remarks = oldAssignee.equals(newAssignee) ? "Assignment updated" : "Contract reassigned";
        createAssignmentHistory(contractDocument, oldAssignee, newAssignee, assignedBy, remarks);
        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    public ContractDocument unassignContract(Long contractId, User user, List<String> roles) {
        String assignedBy = getUserEmail(user);

        ContractDocument contractDocument = getContractOrThrow(contractId, user, roles);
        String oldAssignee = contractDocument.getAssignedTo();
        if (oldAssignee == null || oldAssignee.isBlank()) {
            throw new IllegalStateException("Contract is already unassigned");
        }
        contractDocument.unassign(assignedBy);
        createAssignmentHistory(contractDocument, oldAssignee, null, assignedBy, "Contract unassigned");
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

    @Override
    @Transactional
    public void markAnalysisFailed(
            Long contractId,
            String failureReason,
            User user,
            List<String> roles) {

        ContractDocument contractDocument = getContract(contractId, user, roles);
        ContractAnalysisSummary summary =
                contractAnalysisSummaryRepository
                        .findByContractDocumentId(contractId)
                        .orElseGet(() -> ContractAnalysisSummary.builder()
                                .contractDocument(contractDocument)
                                .build());

        summary.setStatus(AnalysisStatus.FAILED);
        summary.setFailureReason(failureReason);
        summary.setAnalyzedAt(LocalDateTime.now());

        contractAnalysisSummaryRepository.save(summary);

        contractDocument.setStatus(ContractStatus.FAILED);
        contractDocumentRepository.save(contractDocument);
    }

    /**
     * This method will first try to fetch ContractDocumentFieldValue if it founds it updates value there but if not, it will create new field and update its value.
     */
    @Override
    @Transactional
    public ContractDocumentFieldValue fetchAndUpdateContractDocumentFieldValue(
            Long templateFieldId,
            String parsedValue,
            User user) {

        BidTemplateField templateField = getTemplateField(templateFieldId);

        ContractDocumentFieldValue fieldValue = getOrCreateFieldValue(templateField);
        fieldValue.setValue(parsedValue);
        fieldValue.setUpdatedBy(user.getEmail());
        fieldValue.setUpdatedAt(LocalDateTime.now());

        return contractDocumentFieldValueRepository.save(fieldValue);
    }


    private BidTemplateField getTemplateField(
            Long templateFieldId) {

        return bidTemplateFieldRepository
                .findById(templateFieldId)
                .orElseThrow(() ->
                        new BidTemplateFieldNotFoundException(
                                "Template field not found: "
                                        + templateFieldId));
    }

    private ContractDocumentFieldValue getOrCreateFieldValue(BidTemplateField templateField) {

        if (templateField.getFieldValue() != null) {
            return templateField.getFieldValue();
        }
        ContractDocumentFieldValue fieldValue =
                ContractDocumentFieldValue.builder()
                        .templateField(templateField)
                        .build();
        templateField.setFieldValue(fieldValue);
        return fieldValue;
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

    private ContractDocument getContractOrThrow(Long contractId, User user, List<String> roles) {
        validateContractId(contractId);

        ContractDocument contractDocument = contractDocumentRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contract not found with id: " + contractId
                ));
        if (!canAccess(contractDocument, user, roles)) {
            throw new AccessDeniedException(
                    "You are not allowed to access this contract");
        }
        return contractDocument;
    }


    //TODO : NEED TO ADD CONDITION WHERE SELF OR REVIEWERS CAN SEE CONTRACT.
    private boolean canAccess(
            ContractDocument contractDocument,
            User user,
            List<String> roles) {
        boolean admin = roles.stream().anyMatch(a -> a.equals(RoleType.ADMIN.name()));
        if (admin) {
            return true;
        }
        return contractDocument.getStatus().equals(ContractStatus.UPLOADED) &&
                contractDocument.getAssignmentStatus().equals(ContractAssignmentStatus.UNASSIGNED)
                || contractDocument.getAssignedTo().equalsIgnoreCase(user.getEmail());
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