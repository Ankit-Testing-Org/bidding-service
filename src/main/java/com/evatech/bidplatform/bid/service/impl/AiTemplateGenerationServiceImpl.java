package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.ai.service.AiService;
import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.bid.service.BidTemplatePromptBuilder;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.bid.service.AiTemplateGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTemplateGenerationServiceImpl
        implements AiTemplateGenerationService {

    private final BidTemplatePromptBuilder promptBuilder;
    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.mock-ai-enabled:false}")
    private boolean mockAiEnabled;

    @Override
    public AiBidTemplateResponse generateBidTemplate(
            ContractDocument contract,
            List<ContractLot> qualifiedLots
    ) {
        String prompt = promptBuilder.build(contract, qualifiedLots);
        log.info("Generating bid template using AI for contract {}", contract.getId());
        if (mockAiEnabled) {
            return loadSampleResponse("sample-data/bid-template-response.json");
        }
        return aiService.generateBidTemplate(prompt);
    }

    private AiBidTemplateResponse loadSampleResponse(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            return objectMapper.readValue(resource.getInputStream(), AiBidTemplateResponse.class);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load sample response", ex);
        }
    }
}