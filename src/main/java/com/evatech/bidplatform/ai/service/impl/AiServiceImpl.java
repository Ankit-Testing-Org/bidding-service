package com.evatech.bidplatform.ai.service.impl;

import com.evatech.bidplatform.ai.model.AiContractAnalysisResponse;
import com.evatech.bidplatform.ai.service.AiClient;
import com.evatech.bidplatform.ai.service.AiService;
import com.evatech.bidplatform.ai.service.ContractAnalysisPromptBuilder;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractHighlight;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.entity.RiskLevel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
    public List<ContractHighlight> analyseContract(ContractDocument contractDocument, List<ContractPageText> pages) {

        String prompt = contractAnalysisPromptBuilder.buildPrompt(
                contractDocument,
                pages
        );

        String aiResponse = aiClient.analysePrompt(prompt);
        AiContractAnalysisResponse analysisResponse  = parseAiResponse(aiResponse);

        return mapToContractHighlights(contractDocument, analysisResponse);
    }

    private AiContractAnalysisResponse parseAiResponse(String aiResponse) {
        try {
            return objectMapper.readValue(
                    aiResponse,
                    AiContractAnalysisResponse.class
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to parse AI contract analysis response",
                    exception
            );
        }
    }

    private List<ContractHighlight> mapToContractHighlights(
            ContractDocument contractDocument,
            AiContractAnalysisResponse analysisResponse
    ) {
        List<ContractHighlight> highlights = new ArrayList<>();

        if (analysisResponse == null || analysisResponse.highlights() == null) {
            return highlights;
        }

        for (ContractHighlight highlightResponse : analysisResponse.highlights()) {
            ContractHighlight highlight = ContractHighlight.builder()
                    .contractDocument(contractDocument)
                    .category(defaultString(highlightResponse.getCategory(), "GENERAL"))
                    .title(defaultString(highlightResponse.getTitle(), "Contract highlight"))
                    .description(defaultString(highlightResponse.getDescription(), "No description provided"))
                    .pageNumber(highlightResponse.getPageNumber())
                    .riskLevel(parseRiskLevel(highlightResponse.getRiskLevel()))
                    .recommendedAction(defaultString(highlightResponse.getRecommendedAction(), "Review manually"))
                    .confidenceScore(defaultConfidenceScore(highlightResponse.getConfidenceScore()))
                    .build();

            highlights.add(highlight);
        }

        return highlights;
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
