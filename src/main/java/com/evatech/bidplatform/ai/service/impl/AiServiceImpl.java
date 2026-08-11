package com.evatech.bidplatform.ai.service.impl;

import com.evatech.bidplatform.ai.model.AiContractAnalysisResponse;
import com.evatech.bidplatform.ai.service.AiClient;
import com.evatech.bidplatform.ai.service.AiService;
import com.evatech.bidplatform.ai.service.ContractAnalysisPromptBuilder;
import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.contract.dto.ContractLotAnalysisResult;
import com.evatech.bidplatform.contract.dto.response.HighlightReanalysisResponse;
import com.evatech.bidplatform.contract.dto.response.AiContractLotAnalysisResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.*;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.entity.RiskLevel;
import com.evatech.bidplatform.contract.dto.ContractAnalysisResult;
import com.evatech.bidplatform.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiClient aiClient;
    private final ContractAnalysisPromptBuilder contractAnalysisPromptBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ContractAnalysisResult analyseContract(ContractDocument contractDocument, List<ContractPageText> pages) {

        String prompt = contractAnalysisPromptBuilder.buildPrompt(contractDocument, pages);

        String aiResponse = aiClient.analysePrompt(prompt);

        AiContractAnalysisResponse analysisResponse = parseAiResponse(aiResponse);

        List<ContractHighlight> highlights = mapToContractHighlights(contractDocument, analysisResponse);

        ContractAnalysisSummary summary = mapToAnalysisSummary(contractDocument, analysisResponse);

        return ContractAnalysisResult.builder().highlights(highlights).summary(summary).build();
    }

    @Override
    public ContractLotAnalysisResult analyseContractLot(ContractDocument contractDocument,
                                                        ContractLot contractLot,
                                                        List<ContractPageText> lotPages,
                                                        String userComment) {

        String prompt = contractAnalysisPromptBuilder.buildLotPrompt(contractDocument, contractLot, lotPages, userComment);

        String aiResponse = aiClient.analysePrompt(prompt);

        AiContractLotAnalysisResponse analysisResponse = parseLotAiResponse(aiResponse);

        List<ContractLotHighlight> highlights = mapToContractLotHighlights(contractLot, analysisResponse);

        ContractLotAnalysis analysis = mapToContractLotAnalysis(contractLot, analysisResponse);

        return ContractLotAnalysisResult.builder().analysis(analysis).highlights(highlights).build();
    }

    @Override
    public HighlightReanalysisResponse reanalyseHighlight(
            ContractDocument contractDocument,
            ContractHighlight highlight,
            String userComment) {

        String prompt = contractAnalysisPromptBuilder.buildHighlightReanalysisPrompt(
                        contractDocument, highlight, userComment);

        String aiResponse = aiClient.analysePrompt(prompt);

        return parseHighlightReanalysisResponse(aiResponse);
    }

    @Override
    public AiBidTemplateResponse generateText(String prompt) {

        //TODO : NEED TO FIND WAY

        return null;
    }

    @Override
    public void createContractCopyFromTemplate(
            ContractDocument contract,
            Resource uploadedTemplate,
            User user) {

        //TODO : NEED TO FIND WAY
    }

    private HighlightReanalysisResponse parseHighlightReanalysisResponse(
            String aiResponse) {
        try {
            return objectMapper.readValue(aiResponse, HighlightReanalysisResponse.class);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to parse highlight reanalysis response",
                    ex);
        }
    }

    private AiContractLotAnalysisResponse parseLotAiResponse(String aiResponse) {
        try {
            return objectMapper.readValue(aiResponse, AiContractLotAnalysisResponse.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse lot analysis response", ex);
        }
    }

    private ContractLotAnalysis mapToContractLotAnalysis(ContractLot contractLot, AiContractLotAnalysisResponse response) {
        ContractLotAnalysis analysis = new ContractLotAnalysis();
        analysis.setContractLot(contractLot);
        analysis.setRecommendedToBid(response.getRecommendedToBid());
        analysis.setWinProbability(response.getWinProbability());
        analysis.setOverallRiskLevel(response.getOverallRiskLevel());
        analysis.setExecutiveSummary(response.getExecutiveSummary());
        analysis.setRecommendation(response.getRecommendation());
        return analysis;
    }

    private List<ContractLotHighlight> mapToContractLotHighlights(ContractLot contractLot, AiContractLotAnalysisResponse response) {
        if (response.getHighlights() == null) {
            return List.of();
        }
        return response.getHighlights().stream().map(item ->
                ContractLotHighlight.builder().contractLot(contractLot).
                        category(item.getCategory()).title(item.getTitle()).
                        description(item.getDescription()).
                        pageNumber(item.getPageNumber()).riskLevel(item.getRiskLevel()).
                        confidenceScore(item.getConfidenceScore()).
                        recommendedAction(item.getRecommendedAction()).
                        bidCapable(item.getBidCapable()).build()).
                toList();
    }


    private AiContractAnalysisResponse parseAiResponse(String aiResponse) {

        try {
            return objectMapper.readValue(aiResponse, AiContractAnalysisResponse.class);
        } catch (JsonProcessingException exception) {

            throw new IllegalStateException("Failed to parse AI contract analysis response", exception);
        }
    }

    private List<ContractHighlight> mapToContractHighlights(ContractDocument contractDocument, AiContractAnalysisResponse analysisResponse) {

        List<ContractHighlight> highlights = new ArrayList<>();

        if (analysisResponse == null || analysisResponse.highlights() == null) {
            return highlights;
        }

        for (ContractHighlight highlightResponse : analysisResponse.highlights()) {

            ContractHighlight highlight = ContractHighlight.builder().contractDocument(contractDocument).category(highlightResponse.getCategory()).title(defaultString(highlightResponse.getTitle(), "Contract Highlight")).description(defaultString(highlightResponse.getDescription(), "No description provided")).pageNumber(highlightResponse.getPageNumber()).reference(highlightResponse.getReference()).riskLevel(parseRiskLevel(highlightResponse.getRiskLevel())).severityScore(highlightResponse.getSeverityScore()).mandatory(highlightResponse.getMandatory()).recommendedAction(defaultString(highlightResponse.getRecommendedAction(), "Review manually")).confidenceScore(defaultConfidenceScore(highlightResponse.getConfidenceScore())).build();

            highlights.add(highlight);
        }

        return highlights;
    }

    private ContractAnalysisSummary mapToAnalysisSummary(ContractDocument contractDocument, AiContractAnalysisResponse response) {

        if (response == null) {
            return null;
        }

        return ContractAnalysisSummary.builder().contractDocument(contractDocument).clientName(response.clientName()).contractValue(response.contractValue()).currency(response.currency()).submissionDeadline(response.submissionDeadline()).contractStartDate(response.contractStartDate()).contractEndDate(response.contractEndDate()).executiveSummary(response.executiveSummary()).recommendation(response.recommendation()).overallBidScore(response.overallBidScore()).overallRiskLevel(response.overallRiskLevel()).legalScore(response.legalScore()).complianceScore(response.complianceScore()).commercialScore(response.commercialScore()).penaltyScore(response.penaltyScore()).criticalClauses(response.criticalClauses()).mandatoryDocuments(response.mandatoryDocuments()).durationMonths(response.durationMonths()).totalLots(response.totalLots()).qualifiedLots(response.qualifiedLots()).status(AnalysisStatus.COMPLETED).build();
    }

    private RiskLevel parseRiskLevel(RiskLevel riskLevel) {

        if (riskLevel == null) {
            return RiskLevel.LOW;
        }

        try {
            return RiskLevel.valueOf(riskLevel.name().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return RiskLevel.LOW;
        }
    }

    private String defaultString(String value, String defaultValue) {

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    private Double defaultConfidenceScore(Double confidenceScore) {

        if (confidenceScore == null) {
            return 0.0;
        }

        return confidenceScore;
    }
}
