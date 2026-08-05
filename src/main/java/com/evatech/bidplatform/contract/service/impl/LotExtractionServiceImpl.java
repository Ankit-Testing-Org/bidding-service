package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.contract.dto.ExtractedLotResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractLot;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.entity.LotParticipationStatus;
import com.evatech.bidplatform.contract.repository.ContractLotRepository;
import com.evatech.bidplatform.contract.repository.ContractPageTextRepository;
import com.evatech.bidplatform.contract.service.LotExtractionService;
import com.evatech.bidplatform.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LotExtractionServiceImpl implements LotExtractionService {

    private final ContractPageTextRepository contractPageTextRepository;
    private final ContractLotRepository contractLotRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<ContractLot> extractLots(ContractDocument contractDocument,
                                         User user) {
        validateContractDocument(contractDocument);

        Long contractId = contractDocument.getId();

        List<ContractLot> existingLots =
                contractLotRepository.findByContractDocumentIdOrderByLotNumberAsc(
                        contractId
                );

        if (!existingLots.isEmpty()) {
            return existingLots;
        }

        List<ContractPageText> pages =
                contractPageTextRepository.findByContractDocumentIdOrderByPageNumberAsc(
                        contractId
                );

        if (pages.isEmpty()) {
            throw new IllegalStateException(
                    "No extracted page text found for contract id: " + contractId
            );
        }
        String contractText = buildContractText(pages);
        String prompt = "DUMMY PROMPT"; //TODO :  NEED TO FIX IT
        String aiResponse = "DUMMY RESPONSE "; //TODO :  NEED TO FIX IT
        List<ExtractedLotResponse> extractedLots = parseAiResponse(aiResponse);
        if (extractedLots.isEmpty()) {
            return List.of();
        }
        return saveLots(contractDocument, extractedLots);
    }

    @Override
    public List<ContractLot> extractLots(ContractDocument contractDocument, User user, String lotNumber) {

        validateContractDocument(contractDocument);

        Long contractId = contractDocument.getId();

        if(StringUtils.hasText(lotNumber))
            return contractLotRepository.findByContractDocumentIdAndLotNumber(contractId, lotNumber);

        return contractLotRepository.findByContractDocumentIdOrderByLotNumberAsc(contractId);
    }


    private List<ContractLot> saveLots(
            ContractDocument contractDocument,
            List<ExtractedLotResponse> extractedLots) {

        List<ContractLot> savedLots = new ArrayList<>();

        for (ExtractedLotResponse extractedLot : extractedLots) {
            validateExtractedLot(extractedLot);

            ContractLot contractLot = ContractLot.builder()
                    .contractDocument(contractDocument)
                    .lotNumber(extractedLot.getLotNumber())
                    .lotName(extractedLot.getLotName())
                    .description(extractedLot.getDescription())
                    .startPage(extractedLot.getStartPage())
                    .endPage(extractedLot.getEndPage())
                    .participationStatus(LotParticipationStatus.OPEN)
                    .createdAt(LocalDateTime.now())
                    .build();

            savedLots.add(
                    contractLotRepository.save(contractLot)
            );
        }

        return savedLots;
    }

    private String buildContractText(List<ContractPageText> pages) {
        StringBuilder builder = new StringBuilder();

        for (ContractPageText page : pages) {
            builder.append("\n\n--- PAGE ")
                    .append(page.getPageNumber())
                    .append(" ---\n");

            if (page.getText() != null
                    && !page.getText().isBlank()) {
                builder.append(page.getText());
            }
        }

        return builder.toString();
    }

    private List<ExtractedLotResponse> parseAiResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            throw new IllegalStateException("AI response for lot extraction is empty");
        }

        String cleanedResponse = cleanJsonResponse(aiResponse);

        try {
            return objectMapper.readValue(
                    cleanedResponse,
                    new TypeReference<List<ExtractedLotResponse>>() {
                    }
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to parse lot extraction AI response: " + cleanedResponse,
                    exception
            );
        }
    }

    private String cleanJsonResponse(String aiResponse) {
        String cleanedResponse = aiResponse.trim();

        if (cleanedResponse.startsWith("```json")) {
            cleanedResponse = cleanedResponse.substring(7);
        }

        if (cleanedResponse.startsWith("```")) {
            cleanedResponse = cleanedResponse.substring(3);
        }

        if (cleanedResponse.endsWith("```")) {
            cleanedResponse = cleanedResponse.substring(
                    0,
                    cleanedResponse.length() - 3
            );
        }

        return cleanedResponse.trim();
    }

    private void validateExtractedLot(ExtractedLotResponse extractedLot) {
        if (extractedLot == null) {
            throw new IllegalStateException("Extracted lot must not be null");
        }

        if (extractedLot.getLotNumber() == null
                || extractedLot.getLotNumber().isBlank()) {
            throw new IllegalStateException("Extracted lot number must not be empty");
        }

        if (extractedLot.getLotName() == null
                || extractedLot.getLotName().isBlank()) {
            throw new IllegalStateException(
                    "Extracted lot name must not be empty for lot number: "
                            + extractedLot.getLotNumber()
            );
        }

        if (extractedLot.getStartPage() != null
                && extractedLot.getEndPage() != null
                && extractedLot.getStartPage() > extractedLot.getEndPage()) {
            throw new IllegalStateException(
                    "Lot start page cannot be greater than end page for lot number: "
                            + extractedLot.getLotNumber()
            );
        }
    }

    private void validateContractDocument(ContractDocument contractDocument) {
        if (contractDocument == null) {
            throw new IllegalArgumentException("Contract document must not be null");
        }

        if (contractDocument.getId() == null) {
            throw new IllegalArgumentException("Contract document id must not be null");
        }
    }
}