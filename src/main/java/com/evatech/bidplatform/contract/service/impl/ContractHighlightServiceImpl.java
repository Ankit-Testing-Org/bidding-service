package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.ai.service.AiRequestLoggerService;
import com.evatech.bidplatform.ai.service.AiService;
import com.evatech.bidplatform.audit.service.AuditAction;
import com.evatech.bidplatform.contract.dto.ContractAnalysisResult;
import com.evatech.bidplatform.contract.dto.ContractAnalysisSection;
import com.evatech.bidplatform.contract.dto.response.HighlightReanalysisResponse;
import com.evatech.bidplatform.contract.dto.HighlightReviewStatus;
import com.evatech.bidplatform.contract.entity.*;
import com.evatech.bidplatform.contract.entity.analysis.*;
import com.evatech.bidplatform.contract.repository.*;
import com.evatech.bidplatform.contract.service.ContractHighlightService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.dto.response.ContractAnalysisPageResponse;
import com.evatech.bidplatform.user.dto.response.ContractAnalysisSectionType;
import com.evatech.bidplatform.user.dto.response.ContractHighlightResponse;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.mapper.ContractAnalysisMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ContractHighlightServiceImpl implements ContractHighlightService {

    private final ContractDocumentRepository contractDocumentRepository;
    private final ContractPageTextRepository contractPageTextRepository;
    private final ContractHighlightRepository contractHighlightRepository;
    private final ContractService contractService;
    private final AiService aiService;
    private final AiRequestLoggerService aiRequestLoggerService;
    private final ContractAnalysisSummaryRepository contractAnalysisSummaryRepository;
    private final ContractAnalysisMapper contractAnalysisMapper;

    @AuditAction(action = "ANALYSE_CONTRACT_HISTORY", entity = "")
    @Override
    @Transactional
    public ContractAnalysisPageResponse analyseContractHighlights(Long contractDocumentId, boolean reanalyse, User user, List<String> roles) {

        ContractDocument contractDocument = contractDocumentRepository.findById(contractDocumentId).orElseThrow(() -> new IllegalArgumentException("Contract document not found with id: " + contractDocumentId));

        boolean alreadyAnalysed = contractHighlightRepository.existsByContractDocumentId(contractDocumentId);

        if (alreadyAnalysed && !reanalyse) {
            ContractAnalysisSummary existingSummary = contractAnalysisSummaryRepository.findByContractDocumentId(contractDocumentId).orElseThrow(() -> new IllegalStateException("Analysis summary not found for contract document id: " + contractDocumentId));

            List<ContractHighlight> existingHighlights = contractHighlightRepository.findByContractDocumentIdOrderByPageNumberAsc(contractDocumentId);

            return contractAnalysisMapper.toPageResponse(contractDocument.getId(), existingSummary, existingHighlights);
        }

        List<ContractPageText> pages = contractPageTextRepository.findByContractDocumentIdOrderByPageNumberAsc(contractDocumentId);

        if (pages.isEmpty()) {
            throw new IllegalStateException("Contract text must be extracted before analysis");
        }

        contractService.markAnalysisInProgress(contractDocumentId, user, roles);

        try {
            ContractAnalysisResult analysisResult = aiService.analyseContract(contractDocument, pages);

            aiRequestLoggerService.logRequest(contractDocumentId, contractDocument.getAssignedTo());

            if (alreadyAnalysed || reanalyse) {
                contractHighlightRepository.deleteByContractDocumentId(contractDocumentId);
                contractAnalysisSummaryRepository.deleteByContractDocumentId(contractDocumentId);
            }
            ContractAnalysisSummary savedSummary = saveAnalysisSummary(contractDocument, analysisResult.getSummary());
            List<ContractHighlight> savedHighlights = saveHighlights(contractDocument, analysisResult.getSections());
            contractService.markAnalysed(contractDocumentId, user, roles);
            return contractAnalysisMapper.toPageResponse(contractDocument.getId(), savedSummary, savedHighlights);

        } catch (RuntimeException ex) {
            contractService.markAnalysisFailed(contractDocumentId, ex.getMessage(), user, roles);

            throw ex;
        }
    }

    @AuditAction(action = "APPROVE_HIGHLIGHT", entity = "ContractHighlight")
    @Override
    @Transactional
    public ContractHighlightResponse approveHighlight(Long highlightId, User user) {
        ContractHighlight highlight = contractHighlightRepository.findById(highlightId).orElseThrow();
        ContractHighlightReviewHistory contractHighlightReviewHistory = addReview(highlight,
                HighlightReviewStatus.APPROVED, user.getEmail(), null);
        highlight.getReviewHistory().add(contractHighlightReviewHistory);
        highlight = contractHighlightRepository.save(highlight);
        return contractAnalysisMapper.toHighlightResponse(highlight);
    }

    @AuditAction(
            action = "REJECT_HIGHLIGHT",
            entity = "ContractHighlight"
    )
    @Override
    @Transactional
    public ContractHighlightResponse rejectHighlight(Long highlightId, String comment, User user) {

        ContractHighlight highlight =
                contractHighlightRepository.findById(highlightId).orElseThrow();

        highlight.setReviewStatus(HighlightReviewStatus.REJECTED);

        ContractHighlightReviewHistory reviewHistory =
                addReview(highlight, HighlightReviewStatus.REJECTED, user.getEmail(), comment);

        highlight.getReviewHistory().add(reviewHistory);

        highlight = contractHighlightRepository.save(highlight);

        return contractAnalysisMapper.toHighlightResponse(highlight);
    }

    @AuditAction(action = "FETCH_HIGHLIGHT", entity = "ContractHighlight")
    @Override
    @Transactional(readOnly = true)
    public List<ContractHighlightResponse> getHighlights(Long contractId) {
        List<ContractHighlight> contractHighlights = contractHighlightRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId);
        return contractAnalysisMapper.toHighlightResponses(contractHighlights);

    }

    @AuditAction(action = "REANALYSE_HIGHLIGHT", entity = "ContractHighlight")
    @Override
    @Transactional
    public ContractHighlightResponse reanalyseHighlight(Long highlightId, User user, String userComment) {


        ContractHighlight highlight = contractHighlightRepository.findById(highlightId).orElseThrow();
        ContractDocument contract = highlight.getContractDocument();
        HighlightReanalysisResponse result = aiService.reanalyseHighlight(contract, highlight, userComment);
        highlight.setCategory(result.category());
        highlight.setTitle(result.title());
        highlight.setDescription(result.description());
        highlight.setRiskLevel(result.riskLevel());
        highlight.setSeverityScore(result.severityScore());
        highlight.setRecommendedAction(result.recommendedAction());
        highlight.setConfidenceScore(result.confidenceScore());
        ContractHighlightReviewHistory contractHighlightReviewHistory = addReview(highlight, HighlightReviewStatus.REANALYSE, user.getEmail(), userComment);
        highlight.getReviewHistory().add(contractHighlightReviewHistory);
        highlight = contractHighlightRepository.save(highlight);
        return contractAnalysisMapper.toHighlightResponse(highlight);
    }

    public ContractHighlightReviewHistory addReview(ContractHighlight highlight, HighlightReviewStatus status, String reviewedBy, String reviewComment) {
        highlight.setReviewStatus(status);
        ContractHighlightReviewHistory history = ContractHighlightReviewHistory.builder().contractHighlight(highlight).reviewStatus(status).reviewedBy(reviewedBy).reviewedAt(LocalDateTime.now()).reviewComment(reviewComment).build();

        highlight.getReviewHistory().add(history);
        return history;
    }

    private List<ContractHighlight> saveHighlights(ContractDocument contractDocument, List<ContractAnalysisSection> analysisSections) {

        if (analysisSections == null || analysisSections.isEmpty()) {
            return List.of();
        }

        List<ContractHighlight> highlights = analysisSections.stream().filter(section -> section != null).flatMap(section -> toHighlights(contractDocument, section).stream()).toList();

        if (highlights.isEmpty()) {
            return List.of();
        }

        return contractHighlightRepository.saveAll(highlights);
    }

    private List<ContractHighlight> toHighlights(ContractDocument contractDocument, ContractAnalysisSection section) {

        if (section.getHighlights() == null || section.getHighlights().isEmpty()) {
            return List.of();
        }

        return section.getHighlights().stream().filter(Objects::nonNull).peek(highlight -> prepareHighlight(contractDocument, section, highlight)).toList();
    }

    private void prepareHighlight(ContractDocument contractDocument, ContractAnalysisSection section, ContractHighlight highlight) {

        highlight.setContractDocument(contractDocument);

        if (highlight.getCategory() == null) {
            highlight.setCategory(resolveHighlightCategory(section.getType()));
        }

        if (highlight.getMandatory() == null) {
            highlight.setMandatory(false);
        }

        if (highlight.getSeverityScore() == null) {
            highlight.setSeverityScore(50);
        }

        if (highlight.getReviewStatus() == null) {
            highlight.setReviewStatus(HighlightReviewStatus.PENDING);
        }

        if (highlight.getCreatedAt() == null) {
            highlight.setCreatedAt(LocalDateTime.now());
        }

        if (highlight.getReviewHistory() == null) {
            highlight.setReviewHistory(new ArrayList<>());
        }
    }

    private ContractHighlightCategory resolveHighlightCategory(ContractAnalysisSectionType sectionType) {

        if (sectionType == null) {
            return ContractHighlightCategory.TERMS_AND_CONDITIONS;
        }

        return switch (sectionType) {
            case EXECUTIVE_SUMMARY -> ContractHighlightCategory.EXECUTIVE_SUMMARY;
            case TERMS_AND_CONDITIONS -> ContractHighlightCategory.TERMS_AND_CONDITIONS;
            case COMPLIANCE_REQUIREMENTS -> ContractHighlightCategory.COMPLIANCE_REQUIREMENT;

            case MANDATORY_BIDDER_ACTIONS -> ContractHighlightCategory.MANDATORY_ACTION;

            case AI_RECOMMENDATIONS -> ContractHighlightCategory.AI_RECOMMENDATION;
            case REVIEWER_FEEDBACK_HISTORY -> null;
        };
    }

    private ContractAnalysisSummary saveAnalysisSummary(ContractDocument contractDocument, ContractAnalysisSummary summary) {
        if (summary == null) {
            throw new IllegalStateException("AI did not return contract analysis summary");
        }
        summary.setContractDocument(contractDocument);
        ContractAnalysisSummary saved = contractAnalysisSummaryRepository.save(summary);
        updateContractFromAnalysis(contractDocument, saved);
        return saved;
    }

    private void updateContractFromAnalysis(ContractDocument contractDocument, ContractAnalysisSummary summary) {
        contractDocument.setClientName(summary.getClientName());
        contractDocument.setContractValue(summary.getContractValue());
        contractDocument.setCurrency(summary.getCurrency());
        contractDocument.setSubmissionDeadline(summary.getSubmissionDeadline());
        contractDocument.setStatus(ContractStatus.ANALYSED);
        contractDocumentRepository.save(contractDocument);
    }
}