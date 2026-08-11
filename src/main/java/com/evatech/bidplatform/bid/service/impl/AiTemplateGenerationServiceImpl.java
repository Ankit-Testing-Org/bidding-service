package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.ai.service.AiService;
import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.bid.service.AiTemplateGenerationService;
import com.evatech.bidplatform.bid.service.BidTemplatePromptBuilder;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTemplateGenerationServiceImpl
        implements AiTemplateGenerationService {

    private final AiService aiService;
    private final BidTemplatePromptBuilder promptBuilder;

    @Override
    public AiBidTemplateResponse generateBidTemplate(
            ContractDocument contract,
            List<ContractLot> qualifiedLots) {

        String prompt = promptBuilder.build(
                contract,
                qualifiedLots);

        log.info(
                "Generating bid template for contract {}",
                contract.getId());

        return aiService.generateText(prompt);
    }
}