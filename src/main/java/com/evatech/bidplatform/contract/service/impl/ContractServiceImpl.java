package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.audit.service.AuditAction;
import com.evatech.bidplatform.bid.service.FileStorageService;
import com.evatech.bidplatform.contract.entity.*;
import com.evatech.bidplatform.contract.entity.analysis.AnalysisStatus;
import com.evatech.bidplatform.contract.entity.analysis.ContractAnalysisSummary;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.repository.*;
import com.evatech.bidplatform.contract.service.*;
import com.evatech.bidplatform.dashboard.dto.ProcessingQueueItemResponse;
import com.evatech.bidplatform.dashboard.dto.ProposalRequest;
import com.evatech.bidplatform.dashboard.dto.contract.request.ContractSearchRequest;
import com.evatech.bidplatform.dashboard.service.ProposalService;
import com.evatech.bidplatform.user.dto.RoleType;
import com.evatech.bidplatform.user.dto.response.ContractHighlightResponse;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractDocumentRepository contractDocumentRepository;
    private final ContractAssignmentHistoryRepository contractAssignmentHistoryRepository;
    private final FileStorageService fileStorageService;
    private final ContractLotService contractLotService;
    private final ContractTextExtractionService contractTextExtractionService;
    private final ContractAnalysisSummaryRepository contractAnalysisSummaryRepository;
    private final ContractHighlightService contractHighlightService;
    private final ContractAnalysisOrchestrator contractAnalysisOrchestrator;
    private final ProposalService proposalService;

    @AuditAction(action = "UPLOAD_CONTRACT", entity = "ContractDocument")
    @Override
    public ContractDocument uploadContract(MultipartFile file, User user, List<String> roles) {

        validateFile(file, user.getEmail());

        String storagePath = fileStorageService.storeContract(file);

        ContractDocument contractDocument = ContractDocument.builder().originalFileName(file.getOriginalFilename()).fileType(file.getContentType()).storagePath(storagePath).uploadedBy(user.getEmail()).status(ContractStatus.UPLOADED).assignmentStatus(ContractAssignmentStatus.UNASSIGNED).build();
        contractDocument = contractDocumentRepository.save(contractDocument);

        contractAnalysisOrchestrator.startAnalysis(contractDocument, user, roles);
        return contractDocument;
    }


    @AuditAction(action = "FETCH_CONTRACT", entity = "ContractDocument")
    @Override
    @Transactional(readOnly = true)
    public ContractDocument getContract(Long contractId, User user, List<String> roles) {
        validateUser(user);
        return getContractOrThrow(contractId, user, roles);
    }

    @AuditAction(action = "SAVE_CONTRACT", entity = "ContractDocument")
    @Override
    public ContractDocument saveContract(ContractDocument contractDocument) {
        if (contractDocument == null) {
            throw new IllegalArgumentException("Contract document must not be null");
        }

        return contractDocumentRepository.save(contractDocument);
    }

    @AuditAction(action = "FETCH_CONTRACT_PAGES", entity = "ContractPageText")
    @Override
    @Transactional(readOnly = true)
    public List<ContractPageText> getContractPages(Long contractId, Integer pageNumber, User user, List<String> roles) {

        ContractDocument contractDocument = getContract(contractId, user, roles);
        return contractTextExtractionService.getContractPages(contractDocument, pageNumber, user);
    }

    @AuditAction(action = "FETCH_CONTRACT_LOTS", entity = "ContractLot")
    @Override
    @Transactional(readOnly = true)
    public List<ContractLot> getContractLots(Long contractId, User user, Long lotId, List<String> roles) {
        return contractLotService.extractLots(contractId, user, lotId);
    }

    @AuditAction(action = "FETCH_CONTRACT_HIGHLIGHTS", entity = "ContractHighlight")
    @Override
    @Transactional(readOnly = true)
    public List<ContractHighlightResponse> getHighlights(Long contractId, User user, List<String> roles) {
        validateUser(user);
        validateContractId(contractId);
        return contractHighlightService.getHighlights(contractId);
    }

    @AuditAction(action = "MARK_ANALYSIS_IN_PROGRESS", entity = "ContractDocument")
    @Override
    public ContractDocument markAnalysisInProgress(Long contractId, User user, List<String> roles) {
        ContractDocument contractDocument = getContractOrThrow(contractId, user, roles);
        if (!ContractStatus.TEXT_EXTRACTED.equals(contractDocument.getStatus()) && !ContractStatus.ANALYSED.equals(contractDocument.getStatus())) {
            throw new IllegalStateException("Contract text must be extracted before analysis can start");
        }
        contractDocument.setStatus(ContractStatus.ANALYSIS_IN_PROGRESS);
        return contractDocumentRepository.save(contractDocument);
    }

    @AuditAction(action = "MARK_ANALYSED", entity = "ContractDocument")
    @Override
    public ContractDocument markAnalysed(Long contractId, User user, List<String> roles) {
        ContractDocument contractDocument = getContractOrThrow(contractId, user, roles);
        if (!ContractStatus.ANALYSIS_IN_PROGRESS.equals(contractDocument.getStatus())) {
            throw new IllegalStateException("Contract must be in analysis progress before marking as analysed");
        }
        contractDocument.setStatus(ContractStatus.ANALYSED);
        return contractDocumentRepository.save(contractDocument);
    }

    @AuditAction(action = "FETCH_UNASSIGNED_CONTRACTS", entity = "ContractDocument")
    @Override
    @Transactional(readOnly = true)
    public List<ContractDocument> getUnassignedContracts(User user, List<String> roles) {
        return contractDocumentRepository.findByAssignmentStatus(ContractAssignmentStatus.UNASSIGNED);
    }

    @AuditAction(action = "FETCH_ASSIGNED_CONTRACTS", entity = "ContractDocument")
    @Override
    @Transactional(readOnly = true)
    public List<ContractDocument> getAssignedContracts(String assignedTo, User user, List<String> roles) {
        if (assignedTo == null || assignedTo.isBlank()) {
            throw new IllegalArgumentException("Assigned to must not be empty");
        }
        return contractDocumentRepository.findByAssignedToAndAssignmentStatus(assignedTo, ContractAssignmentStatus.ASSIGNED);
    }

    @AuditAction(action = "ASSIGN_CONTRACT", entity = "ContractDocument")
    @Override
    public ContractDocument assignContract(Long contractId, String assignedTo, User user, List<String> roles, String userComment) {

        String assignedBy = getUserEmail(user);
        if (assignedTo == null || assignedTo.isBlank()) {
            assignedTo = assignedBy;
        }
        ContractDocument contractDocument = getContractOrThrow(contractId, user, roles);
        String oldAssignee = contractDocument.getAssignedTo();
        contractDocument.assignTo(assignedTo, assignedBy);
        String remarks = resolveAssignmentRemarks(oldAssignee, assignedTo);
        createAssignmentHistory(contractDocument, oldAssignee, assignedTo, assignedBy, remarks);
        contractDocument = contractDocumentRepository.save(contractDocument);

        List<ContractLot> contractLots = contractLotService.getContractLots(contractId, user, roles);
        List<Long> contractLotIds = contractLots.stream().map(ContractLot::getId).toList();

        ProposalRequest proposalRequest = new ProposalRequest(contractDocument.getOriginalFileName(), LocalDate.now(), contractDocument.getId(), contractLotIds, null);
        proposalService.createProposal(contractDocument, user, proposalRequest);
        return contractDocument;
    }

    @AuditAction(action = "REASSIGN_CONTRACT", entity = "ContractDocument")
    @Override
    public ContractDocument reassignContract(Long contractId, String newAssignee, User user, List<String> roles, String userComment) {

        if (newAssignee == null || newAssignee.isBlank()) {
            throw new IllegalArgumentException("New assignee must not be empty");
        }
        String assignedBy = getUserEmail(user);
        ContractDocument contractDocument = getContractOrThrow(contractId, user, roles);
        String oldAssignee = contractDocument.getAssignedTo();
        if (oldAssignee == null || oldAssignee.isBlank()) {
            throw new IllegalStateException("Contract is not currently assigned. Please assign it first.");
        }
        contractDocument.reassignTo(newAssignee, assignedBy);
        String remarks = oldAssignee.equals(newAssignee) ? "Assignment updated" : "Contract reassigned";
        createAssignmentHistory(contractDocument, oldAssignee, newAssignee, assignedBy, remarks);
        return contractDocumentRepository.save(contractDocument);
    }

    @AuditAction(action = "UNASSIGN_CONTRACT", entity = "ContractDocument")
    @Override
    public ContractDocument unassignContract(Long contractId, User user, List<String> roles, String userComment) {
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

    @AuditAction(action = "FETCH_ASSIGNMENT_HISTORY", entity = "ContractAssignmentHistory")
    @Override
    @Transactional(readOnly = true)
    public List<ContractAssignmentHistory> getAssignmentHistory(Long contractId, User user) {
        validateUser(user);
        validateContractId(contractId);

        return contractAssignmentHistoryRepository.findByContractDocumentIdOrderByAssignedAtDesc(contractId);
    }

    private void createAssignmentHistory(ContractDocument contractDocument, String oldAssignee, String newAssignee, String assignedBy, String remarks) {

        ContractAssignmentHistory history = ContractAssignmentHistory.builder().contractDocument(contractDocument).oldAssignee(oldAssignee).newAssignee(newAssignee).assignedBy(assignedBy).assignedAt(LocalDateTime.now()).remarks(remarks).build();

        contractAssignmentHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void markAnalysisFailed(Long contractId, String failureReason, User user, List<String> roles) {

        ContractDocument contractDocument = getContract(contractId, user, roles);
        ContractAnalysisSummary summary = contractAnalysisSummaryRepository.findByContractDocumentId(contractId).orElseGet(() -> ContractAnalysisSummary.builder().contractDocument(contractDocument).build());

        summary.setStatus(AnalysisStatus.FAILED);
        summary.setFailureReason(failureReason);
        summary.setAnalyzedAt(LocalDateTime.now());

        contractAnalysisSummaryRepository.save(summary);

        contractDocument.setStatus(ContractStatus.FAILED);
        contractDocumentRepository.save(contractDocument);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ContractDocument> getContractDashboard() {
        return contractDocumentRepository.findAllByOrderByUploadedAtDesc();
    }

    @Override
    public Page<ContractDocument> searchContracts(User user, List<String> roles, ContractSearchRequest request) {

        int page = request.page() == null || request.page() < 0 ? 0 : request.page();

        int size = request.size() == null || request.size() <= 0 ? 20 : request.size();

        String sortBy = request.sortBy() == null || request.sortBy().isBlank() ?
                "uploadedAt" : request.sortBy();

        Sort.Direction direction = "ASC".equalsIgnoreCase(request.sortDirection()) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<ContractDocument> specification =
                ContractDocumentSpecification.search(request, user.getEmail());

        return contractDocumentRepository.findAll(specification, pageable);
    }

    @Override
    public Resource retrieveContractPdf(Long contractId, User user, List<String> roles) {

        ContractDocument contractDocument = getContract(contractId, user, roles);
        return fileStorageService.loadContract(contractDocument.getStoragePath());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessingQueueItemResponse> getProcessingQueue(
            User user,
            List<String> roles
    ) {

        List<ContractDocument> contracts =
                contractDocumentRepository.findByUploadedByAndStatusInOrderByUploadedAtDesc(
                        user.getEmail(),
                        List.of(
                                ContractStatus.UPLOADED,
                                ContractStatus.TEXT_EXTRACTED,
                                ContractStatus.ANALYSIS_IN_PROGRESS,
                                ContractStatus.ANALYSED,
                                ContractStatus.SUBMITTED_FOR_REVIEW,
                                ContractStatus.FAILED
                                )
                        );

        return contracts.stream()
                .map(this::mapToProcessingItem)
                .toList();
    }

    private String resolveAssignmentRemarks(String oldAssignee, String newAssignee) {

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

        ContractDocument contractDocument = contractDocumentRepository.findById(contractId).orElseThrow(() -> new IllegalArgumentException("Contract not found with id: " + contractId));
        if (!canAccess(contractDocument, user, roles)) {
            throw new AccessDeniedException("You are not allowed to access this contract");
        }
        return contractDocument;
    }


    //TODO : NEED TO ADD CONDITION WHERE SELF OR REVIEWERS CAN SEE CONTRACT.
    private boolean canAccess(ContractDocument contractDocument, User user, List<String> roles) {
        boolean admin = roles.stream().anyMatch(a -> a.equals(RoleType.ADMIN.name()));
        if (admin) {
            return true;
        }
        return contractDocument.getStatus().equals(ContractStatus.UPLOADED) && contractDocument.getAssignmentStatus().equals(ContractAssignmentStatus.UNASSIGNED) || contractDocument.getAssignedTo().equalsIgnoreCase(user.getEmail());
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

    private ProcessingQueueItemResponse mapToProcessingItem(
            ContractDocument contract
    ) {

        return new ProcessingQueueItemResponse(
                contract.getId(),
                contract.getOriginalFileName(),
                contract.getStatus(),
                calculateProgress(
                        contract.getStatus()
                ),
                determineCurrentStep(
                        contract.getStatus()
                ),
                contract.getUploadedAt()
        );
    }

    private String determineCurrentStep(
            ContractStatus status
    ) {
        return switch (status) {

            case UPLOADED -> "Contract Uploaded";

            case TEXT_EXTRACTED -> "Text Extraction Completed";

            case ANALYSIS_IN_PROGRESS -> "AI Analysis Running";

            case ANALYSED -> "Ready For Review";

            case SUBMITTED_FOR_REVIEW -> "Pending Reviewer Decision";

            case APPROVED -> "Approved";

            case REJECTED -> "Rejected";

            case FAILED -> "Processing Failed";
        };
    }

    private Integer calculateProgress(
            ContractStatus status
    ) {
        return switch (status) {

            case UPLOADED -> 10;
            case TEXT_EXTRACTED -> 30;
            case ANALYSIS_IN_PROGRESS -> 60;
            case ANALYSED -> 80;
            case SUBMITTED_FOR_REVIEW -> 95;
            case APPROVED -> 100;
            case REJECTED,
                 FAILED -> 100;
        };
    }
}