package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.ai.service.AiRequestLoggerService;
import com.evatech.bidplatform.ai.service.AiService;
import com.evatech.bidplatform.audit.service.AuditAction;
import com.evatech.bidplatform.contract.dto.response.contract.ContractAnalysisPageResponse;
import com.evatech.bidplatform.contract.dto.response.contract.ContractAnalysisSectionType;
import com.evatech.bidplatform.contract.dto.response.highlight.*;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import com.evatech.bidplatform.contract.entity.analysis.ContractAnalysisSummary;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlight;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlightCategory;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlightReviewHistory;
import com.evatech.bidplatform.contract.repository.ContractAnalysisSummaryRepository;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import com.evatech.bidplatform.contract.repository.ContractHighlightRepository;
import com.evatech.bidplatform.contract.repository.ContractPageTextRepository;
import com.evatech.bidplatform.contract.service.ContractHighlightService;
import com.evatech.bidplatform.contract.service.ContractLifecycleService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.mapper.ContractAnalysisMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ContractHighlightServiceImpl implements ContractHighlightService {

    private final ContractDocumentRepository contractDocumentRepository;
    private final ContractPageTextRepository contractPageTextRepository;
    private final ContractHighlightRepository contractHighlightRepository;
    private final ContractLifecycleService contractLifecycleService;
    private final AiService aiService;
    private final AiRequestLoggerService aiRequestLoggerService;
    private final ContractAnalysisSummaryRepository contractAnalysisSummaryRepository;
    private final ContractAnalysisMapper contractAnalysisMapper;
    private final ObjectMapper objectMapper;

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

        contractLifecycleService.markAnalysisInProgress(contractDocumentId, user, roles);

        try {
            ContractAnalysisResult analysisResult = aiService.analyseContract(contractDocument, pages);

            aiRequestLoggerService.logRequest(contractDocumentId, user.getEmail());

            if (alreadyAnalysed || reanalyse) {
                contractHighlightRepository.deleteByContractDocumentId(contractDocumentId);
                contractAnalysisSummaryRepository.deleteByContractDocumentId(contractDocumentId);
            }
            ContractAnalysisSummary savedSummary = saveAnalysisSummary(contractDocument, analysisResult.getSummary());
            if(savedSummary != null) {
                try {
                    String savedSummaryAsString = objectMapper.writeValueAsString(savedSummary);
                    log.info("savedSummaryAsString : "+savedSummaryAsString);
                } catch (JsonProcessingException e) {
                    log.info("Caught error while processing savedSummaryAsString");
                }
            }
            List<ContractHighlight> savedHighlights = saveHighlights(contractDocument, analysisResult.getSections());
            try {
                String savedHighlightsAsString = objectMapper.writeValueAsString(savedHighlights);
                log.info("savedHighlightsAsString : "+savedHighlightsAsString);
            } catch (JsonProcessingException e) {
                log.info("Caught error while processing savedHighlightsAsString");
            }
            contractLifecycleService.markAnalysed(contractDocumentId, user, roles);
            return contractAnalysisMapper.toPageResponse(contractDocument.getId(), savedSummary, savedHighlights);

        } catch (RuntimeException ex) {
            contractLifecycleService.markAnalysisFailed(contractDocumentId, ex.getMessage(), user, roles);
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

        List<ContractHighlight> highlights = analysisSections.stream().filter(Objects::nonNull).flatMap(section -> toHighlights(contractDocument, section).stream()).toList();

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