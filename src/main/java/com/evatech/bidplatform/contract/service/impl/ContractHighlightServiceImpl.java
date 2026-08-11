package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.ai.service.AiRequestLoggerService;
import com.evatech.bidplatform.ai.service.AiService;
import com.evatech.bidplatform.contract.dto.ContractAnalysisResult;
import com.evatech.bidplatform.contract.dto.response.HighlightReanalysisResponse;
import com.evatech.bidplatform.contract.dto.HighlightReviewStatus;
import com.evatech.bidplatform.contract.entity.*;
import com.evatech.bidplatform.contract.entity.analysis.*;
import com.evatech.bidplatform.contract.repository.*;
import com.evatech.bidplatform.contract.service.ContractHighlightService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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


    @Override
    @Transactional
    public List<ContractHighlight> analyseContractHightlights(Long contractId, boolean reanalyse, User user, List<String> roles) {

        ContractDocument contractDocument = contractDocumentRepository.findById(contractId).orElseThrow(() -> new IllegalArgumentException("Contract not found with id: " + contractId));
        boolean alreadyAnalysed = contractHighlightRepository.existsByContractDocumentId(contractId);
        if (alreadyAnalysed && !reanalyse) {
            return contractHighlightRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId);
        }
        List<ContractPageText> pages = contractPageTextRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId);
        if (pages.isEmpty()) {
            throw new IllegalStateException("Contract text must be extracted before analysis");
        }
        contractService.markAnalysisInProgress(contractId, user, roles);
        try {
            ContractAnalysisResult analysisResult = aiService.analyseContract(contractDocument, pages);
            aiRequestLoggerService.logRequest(contractId, contractDocument.getAssignedTo());
            if (alreadyAnalysed || reanalyse) {
                contractHighlightRepository.deleteByContractDocumentId(contractId);
                contractAnalysisSummaryRepository.deleteByContractDocumentId(contractId);
            }
            List<ContractHighlight> savedHighlights = saveHighlights(contractDocument, analysisResult.getHighlights());
            saveAnalysisSummary(contractDocument, analysisResult.getSummary());
            contractService.markAnalysed(contractId, user, roles);
            return savedHighlights;
        } catch (RuntimeException ex) {
            contractService.markAnalysisFailed(contractId, ex.getMessage(), user, roles);
            throw ex;
        }
    }

    @Transactional
    public ContractHighlight approveHighlight(Long highlightId, User user) {
        ContractHighlight highlight = contractHighlightRepository.findById(highlightId).orElseThrow();
        highlight.setReviewStatus(HighlightReviewStatus.APPROVED);
        highlight.setReviewedBy(user.getEmail());
        highlight.setReviewedAt(LocalDateTime.now());

        return contractHighlightRepository.save(highlight);
    }

    @Transactional
    public ContractHighlight rejectHighlight(Long highlightId, String comment, User user) {

        ContractHighlight highlight = contractHighlightRepository.findById(highlightId)
                        .orElseThrow();
        highlight.setReviewStatus(HighlightReviewStatus.REJECTED);
        highlight.setReviewComment(comment);
        highlight.setReviewedBy(user.getEmail());
        highlight.setReviewedAt(LocalDateTime.now());
        return contractHighlightRepository.save(highlight);
    }

    @Transactional
    public ContractHighlight requestReanalysis(Long highlightId, String comment, User user) {

        ContractHighlight highlight = contractHighlightRepository.findById(highlightId)
                        .orElseThrow();
        highlight.setReviewStatus(HighlightReviewStatus.REANALYSE);
        highlight.setReviewComment(comment);
        highlight.setReviewedBy(user.getEmail());
        highlight.setReviewedAt(LocalDateTime.now());
        return contractHighlightRepository.save(highlight);
    }

    @Transactional
    public ContractHighlight reanalyseHighlight(Long highlightId, User user,
                                                String userComment) {

        ContractHighlight highlight = contractHighlightRepository.findById(highlightId)
                        .orElseThrow();
        ContractDocument contract = highlight.getContractDocument();
        HighlightReanalysisResponse result = aiService.reanalyseHighlight(contract,
                highlight, userComment);
        highlight.setCategory(result.category());
        highlight.setTitle(result.title());
        highlight.setDescription(result.description());
        highlight.setRiskLevel(result.riskLevel());
        highlight.setSeverityScore(result.severityScore());
        highlight.setRecommendedAction(result.recommendedAction());
        highlight.setConfidenceScore(result.confidenceScore());
        return contractHighlightRepository.save(highlight);
    }

    private List<ContractHighlight> saveHighlights(ContractDocument contractDocument, List<ContractHighlight> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return List.of();
        }
        highlights.forEach(highlight -> {
            highlight.setContractDocument(contractDocument);
            highlight.setUserComment(null);
            highlight.setReviewStatus(HighlightReviewStatus.PENDING);
        });

        return contractHighlightRepository.saveAll(highlights);
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