package com.evatech.bidplatform.ai.service.impl;

import com.evatech.bidplatform.ai.model.AiContractAnalysisResponse;
import com.evatech.bidplatform.ai.model.AiContractHighlightResponse;
import com.evatech.bidplatform.ai.service.AiClient;
import com.evatech.bidplatform.ai.service.AiService;
import com.evatech.bidplatform.ai.service.ContractAnalysisPromptBuilder;
import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.contract.dto.response.highlight.ContractAnalysisSection;
import com.evatech.bidplatform.contract.dto.response.lot.ContractLotAnalysisResult;
import com.evatech.bidplatform.contract.dto.response.highlight.HighlightReviewStatus;
import com.evatech.bidplatform.contract.dto.response.highlight.HighlightReanalysisResponse;
import com.evatech.bidplatform.contract.dto.response.lot.AiContractLotAnalysisResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.*;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.dto.response.highlight.ContractAnalysisResult;
import com.evatech.bidplatform.contract.dto.response.contract.ContractAnalysisSectionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiClient aiClient;
    private final ContractAnalysisPromptBuilder contractAnalysisPromptBuilder;

    @Value("${app.mock-ai-enabled:false}")
    private boolean mockAiEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ContractAnalysisResult analyseContract(ContractDocument contractDocument, List<ContractPageText> pages) {
        String prompt = contractAnalysisPromptBuilder.buildPrompt(contractDocument, pages);
        String aiResponse = "";
        if(mockAiEnabled)
            aiResponse = loadResourceFile("sample-data/contract-analysis.json");
        else
            aiResponse = aiClient.analysePrompt(prompt);

        AiContractAnalysisResponse analysisResponse = parseAiResponse(aiResponse);

        ContractAnalysisSummary summary = mapToAnalysisSummary(contractDocument, analysisResponse);
        List<ContractAnalysisSection> sections = mapToSections(contractDocument, analysisResponse);

        return ContractAnalysisResult.builder().
                summary(summary).
                sections(sections).
                build();
    }

    @Override
    public ContractLotAnalysisResult analyseContractLot(ContractDocument contractDocument, ContractLot contractLot, List<ContractPageText> lotPages, String userComment) {

        String prompt = contractAnalysisPromptBuilder.buildLotPrompt(contractDocument, contractLot, lotPages, userComment);
        String aiResponse = "";
        if(mockAiEnabled)
            aiResponse = loadResourceFile("sample-data/contract-analysis.json");
        else
            aiResponse = aiClient.analysePrompt(prompt);
        AiContractLotAnalysisResponse analysisResponse = parseLotAiResponse(aiResponse);
        List<ContractLotHighlight> highlights = mapToContractLotHighlights(contractLot,
                analysisResponse);
        ContractLotAnalysis analysis = mapToContractLotAnalysis(contractLot,
                analysisResponse);
        return ContractLotAnalysisResult.builder().analysis(analysis).highlights(highlights).build();
    }

    @Override
    public HighlightReanalysisResponse reanalyseHighlight(ContractDocument contractDocument, ContractHighlight highlight, String userComment) {

        String prompt = contractAnalysisPromptBuilder.
                buildHighlightReanalysisPrompt(contractDocument, highlight, userComment);

        String aiResponse = "";
        if(mockAiEnabled)
            aiResponse = loadResourceFile("sample-data/contract-analysis.json");
        else
            aiResponse = aiClient.analysePrompt(prompt);

        return parseHighlightReanalysisResponse(aiResponse);
    }

    @Override
    public AiBidTemplateResponse generateBidTemplate(String prompt) {
        return null;
    }


    private HighlightReanalysisResponse parseHighlightReanalysisResponse(String aiResponse) {
        try {
            return objectMapper.readValue(aiResponse, HighlightReanalysisResponse.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse highlight reanalysis response", ex);
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
        return response.getHighlights().stream().
                map(item ->
                        ContractLotHighlight.builder().
                                contractLot(contractLot).
                                category(item.getCategory()).
                                title(item.getTitle()).
                                description(item.getDescription()).
                                pageNumber(item.getPageNumber()).
                                riskLevel(item.getRiskLevel()).
                                confidenceScore(item.getConfidenceScore()).
                                recommendedAction(item.getRecommendedAction()).
                                bidCapable(item.getBidCapable()).
                                build()).
                toList();
    }


    private AiContractAnalysisResponse parseAiResponse(String aiResponse) {

        try {
            return objectMapper.readValue(aiResponse, AiContractAnalysisResponse.class);
        } catch (JsonProcessingException exception) {

            throw new IllegalStateException("Failed to parse AI contract analysis response", exception);
        }
    }

    private List<ContractAnalysisSection> mapToSections(ContractDocument contractDocument,
                                                        AiContractAnalysisResponse response) {

        List<AiContractHighlightResponse> aiHighlights =
                response.highlights() == null ? List.of() : response.highlights();

        return List.of(buildSection(contractDocument,
                aiHighlights,
                ContractAnalysisSectionType.EXECUTIVE_SUMMARY,
                ContractHighlightCategory.EXECUTIVE_SUMMARY,
                "AI Executive Summary",
                "Summary and high-level review points"),
                buildSection(contractDocument,
                        aiHighlights,
                        ContractAnalysisSectionType.TERMS_AND_CONDITIONS,
                        ContractHighlightCategory.TERMS_AND_CONDITIONS,
                        "Terms & Conditions Analysis",
                        "Review AI findings and jump directly to PDF pages"),
                buildSection(contractDocument,
                        aiHighlights,
                        ContractAnalysisSectionType.COMPLIANCE_REQUIREMENTS,
                        ContractHighlightCategory.COMPLIANCE_REQUIREMENT,
                        "Compliance Requirements",
                        "Mandatory documents and declarations required before submission"),
                buildSection(contractDocument,
                        aiHighlights,
                        ContractAnalysisSectionType.MANDATORY_BIDDER_ACTIONS,
                        ContractHighlightCategory.MANDATORY_ACTION,
                        "Mandatory Bidder Actions",
                        "Actions required before bid submission or contract execution"),
                buildSection(contractDocument,
                        aiHighlights,
                        ContractAnalysisSectionType.AI_RECOMMENDATIONS,
                        ContractHighlightCategory.AI_RECOMMENDATION,
                        "AI Recommendations",
                        "Suggested actions based on contract-level analysis"));
    }

    private ContractAnalysisSection buildSection(ContractDocument contractDocument,
                                                 List<AiContractHighlightResponse> aiHighlights,
                                                 ContractAnalysisSectionType sectionType,
                                                 ContractHighlightCategory category,
                                                 String title,
                                                 String subtitle) {

        List<ContractHighlight> highlights = aiHighlights.stream().
                filter(item -> item.category() == category).
                map(item -> mapToContractHighlight(contractDocument, item, category)).
                toList();

        return ContractAnalysisSection.builder().
                type(sectionType).
                title(title).
                subtitle(subtitle).
                highlights(highlights).
                build();
    }

    private ContractHighlight mapToContractHighlight(ContractDocument contractDocument, AiContractHighlightResponse item, ContractHighlightCategory category) {

        return ContractHighlight.builder().
                contractDocument(contractDocument).
                category(item.category() != null ? item.category() : category).
                title(item.title()).
                description(item.description()).
                pageNumber(item.pageNumber()).
                reference(item.reference()).
                riskLevel(item.riskLevel()).
                severityScore(item.severityScore()).
                recommendedAction(item.recommendedAction()).
                mandatory(item.mandatory()).
                confidenceScore(item.confidenceScore()).
                reviewStatus(HighlightReviewStatus.PENDING).
                build();
    }

    private ContractAnalysisSummary mapToAnalysisSummary(ContractDocument contractDocument, AiContractAnalysisResponse response) {

        if (response == null) {
            return null;
        }
        return ContractAnalysisSummary.builder().
                contractDocument(contractDocument).
                clientName(response.clientName()).
                contractValue(response.contractValue()).
                currency(response.currency()).
                submissionDeadline(response.submissionDeadline()).
                contractStartDate(response.contractStartDate()).
                contractEndDate(response.contractEndDate()).
                executiveSummary(response.executiveSummary()).
                recommendation(response.recommendation()).
                overallBidScore(response.overallBidScore()).
                overallRiskLevel(response.overallRiskLevel()).
                legalScore(response.legalScore()).
                complianceScore(response.complianceScore()).
                commercialScore(response.commercialScore()).
                penaltyScore(response.penaltyScore()).
                criticalClauses(response.criticalClauses()).
                mandatoryDocuments(response.mandatoryDocuments()).
                durationMonths(response.durationMonths()).
                totalLots(response.totalLots()).
                qualifiedLots(response.qualifiedLots()).
                status(AnalysisStatus.COMPLETED).build();
    }

    private String loadResourceFile(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, ex);
        }
    }
}
