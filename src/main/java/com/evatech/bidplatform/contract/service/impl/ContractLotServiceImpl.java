package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.ai.service.AiRequestLoggerService;
import com.evatech.bidplatform.ai.service.AiService;
import com.evatech.bidplatform.contract.dto.ContractLotAnalysisResult;
import com.evatech.bidplatform.contract.dto.ExtractedLotResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.entity.LotQualificationStatus;
import com.evatech.bidplatform.contract.entity.analysis.ContractLotAnalysis;
import com.evatech.bidplatform.contract.entity.analysis.ContractLotHighlight;
import com.evatech.bidplatform.contract.repository.ContractLotAnalysisRepository;
import com.evatech.bidplatform.contract.repository.ContractLotHighlightRepository;
import com.evatech.bidplatform.contract.repository.ContractLotRepository;
import com.evatech.bidplatform.contract.repository.ContractPageTextRepository;
import com.evatech.bidplatform.contract.service.ContractLotService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ContractLotServiceImpl implements ContractLotService {

    private final ContractPageTextRepository contractPageTextRepository;
    private final ContractLotRepository contractLotRepository;
    private final ObjectMapper objectMapper;
    private final ContractService contractService;
    private final ContractLotAnalysisRepository contractLotAnalysisRepository;
    private final ContractLotHighlightRepository contractLotHighlightRepository;
    private final AiService aiService;
    private final AiRequestLoggerService aiRequestLoggerService;
    private final EntityManager entityManager;

    @Override
    public ContractLot qualifyLot(Long contractId, String lotNumber, User user) {

        ContractLot contractLot = getContractLotOrThrow(contractId, lotNumber, user);

        contractLot.qualify(user.getEmail());

        return contractLotRepository.save(contractLot);
    }

    @Override
    public ContractLot unqualifyLot(Long contractId, String lotNumber, User user) {
        ContractLot contractLot = getContractLotOrThrow(contractId, lotNumber, user);
        contractLot.unqualify(user.getEmail());
        return contractLotRepository.save(contractLot);
    }

    @Override
    @Transactional
    public List<ContractLot> getContractLots(Long contractId, User user, List<String> roles) {
        contractService.getContract(contractId, user, roles);
        return contractLotRepository.findByContractDocumentIdOrderByLotNumberAsc(contractId);
    }

    @Override
    @Transactional
    public List<ContractLot> getQualifiedLots(Long contractId, User user) {
        getContractLotOrThrow(contractId, null, user);
        return contractLotRepository.findByContractDocumentIdAndQualificationStatus(contractId, LotQualificationStatus.QUALIFIED);
    }

    @Override
    @Transactional
    public List<ContractLot> getUnqualifiedLots(Long contractId, User user) {
        getContractLotOrThrow(contractId, null, user);
        return contractLotRepository.findByContractDocumentIdAndQualificationStatus(contractId, LotQualificationStatus.UNQUALIFIED);
    }

    private ContractLot getContractLotOrThrow(Long contractId, String lotNumber, User user) {

        getContractLotOrThrow(contractId, lotNumber, user);
        return contractLotRepository.findByContractDocumentIdAndLotNumber(contractId, lotNumber).orElseThrow(() -> new IllegalArgumentException("Lot not found with contract id " + contractId + " and lot number " + lotNumber));
    }

    @Override
    public List<ContractLot> extractLots(Long contractId, User user, String lotNumber) {

        getContractLotOrThrow(contractId, lotNumber, user);

        if (StringUtils.hasText(lotNumber))
            return Collections.singletonList(contractLotRepository.findByContractDocumentIdAndLotNumber(contractId, lotNumber).orElseThrow(() -> new IllegalArgumentException("Lot not found with contract id " + contractId + " and lot number " + lotNumber)));

        return contractLotRepository.findByContractDocumentIdOrderByLotNumberAsc(contractId);
    }

    @Override
    public List<ContractLot> processExtractingLots(ContractDocument contractDocument, User user) {
        validateContractDocument(contractDocument);

        Long contractId = contractDocument.getId();

        List<ContractLot> existingLots = contractLotRepository.findByContractDocumentIdOrderByLotNumberAsc(contractId);

        if (!existingLots.isEmpty()) {
            return existingLots;
        }

        List<ContractPageText> pages = contractPageTextRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId);

        if (pages.isEmpty()) {
            throw new IllegalStateException("No extracted page text found for contract id: " + contractId);
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
    @Transactional
    public ContractLotAnalysisResult analyseContractLot(Long contractLotId,
                                                        boolean reanalyse,
                                                        User user, List<String> roles,
                                                        String userComment) {
        ContractLot contractLot = contractLotRepository.findById(contractLotId).orElseThrow(() -> new IllegalArgumentException("Contract lot not found with id: " + contractLotId));
        ContractDocument contractDocument = contractLot.getContractDocument();
        boolean alreadyAnalysed = contractLotAnalysisRepository.existsByContractLotId(contractLotId) || contractLotHighlightRepository.existsByContractLotId(contractLotId);
        if (alreadyAnalysed && !reanalyse) {
            return getExistingLotAnalysisResult(contractLotId);
        }
        List<ContractPageText> lotPages = resolveLotPages(contractLot);
        if (lotPages.isEmpty() && isBlank(contractLot.getDescription())) {
            throw new IllegalStateException("Lot text must be available before analysis. Either start/end pages or lot description is required.");
        }
        contractLot.markAnalysisInProgress();
        try {
            ContractLotAnalysisResult analysisResult = aiService.analyseContractLot(contractDocument, contractLot, lotPages,
                    userComment);
            aiRequestLoggerService.logRequest(contractDocument.getId(), contractDocument.getAssignedTo());
            if (alreadyAnalysed || reanalyse) {
                contractLotHighlightRepository.deleteByContractLotId(contractLotId);
                contractLotAnalysisRepository.deleteByContractLotId(contractLotId);
                entityManager.flush();
            }
            ContractLotAnalysis savedAnalysis = saveLotAnalysis(contractLot, analysisResult.getAnalysis());
            List<ContractLotHighlight> savedHighlights = saveLotHighlights(contractLot, analysisResult.getHighlights());
            contractLot.markAnalysed();
            return ContractLotAnalysisResult.builder().analysis(savedAnalysis).highlights(savedHighlights).build();
        } catch (RuntimeException ex) {
            contractLot.markAnalysisFailed(ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    @Override
    public ContractLotAnalysisResult reanalyseLot(Long lotId, String userComment, User user, List<String> roles) {
        ContractLot lot = contractLotRepository.findById(lotId).orElseThrow();
        lot.requestReanalysis(user.getEmail(), userComment);
        return analyseContractLot(lotId, true, user, roles, userComment);
    }


    private ContractLotAnalysisResult getExistingLotAnalysisResult(Long contractLotId) {
        ContractLotAnalysis existingAnalysis = contractLotAnalysisRepository.findByContractLotId(contractLotId).orElse(null);

        List<ContractLotHighlight> existingHighlights = contractLotHighlightRepository.findByContractLotIdOrderByPageNumberAsc(contractLotId);

        return ContractLotAnalysisResult.builder().analysis(existingAnalysis).highlights(existingHighlights).build();
    }

    private List<ContractPageText> resolveLotPages(ContractLot contractLot) {
        Long contractDocumentId = contractLot.getContractDocument().getId();

        if (contractLot.getStartPage() == null || contractLot.getEndPage() == null) {
            return new ArrayList<>();
        }

        if (contractLot.getStartPage() > contractLot.getEndPage()) {
            throw new IllegalStateException("Invalid lot page range. Start page cannot be greater than end page.");
        }

        return contractPageTextRepository.findByContractDocumentIdAndPageNumberBetweenOrderByPageNumberAsc(contractDocumentId, contractLot.getStartPage(), contractLot.getEndPage());
    }

    private ContractLotAnalysis saveLotAnalysis(ContractLot contractLot, ContractLotAnalysis analysis) {
        if (analysis == null) {
            return null;
        }

        ContractLotAnalysis lotAnalysis = new ContractLotAnalysis();
        lotAnalysis.setContractLot(contractLot);
        lotAnalysis.setRecommendedToBid(analysis.getRecommendedToBid());
        lotAnalysis.setWinProbability(analysis.getWinProbability());
        lotAnalysis.setOverallRiskLevel(analysis.getOverallRiskLevel());
        lotAnalysis.setExecutiveSummary(analysis.getExecutiveSummary());
        lotAnalysis.setRecommendation(analysis.getRecommendation());

        ContractLotAnalysis savedAnalysis = contractLotAnalysisRepository.save(lotAnalysis);

        contractLot.setAnalysis(savedAnalysis);

        return savedAnalysis;
    }

    private List<ContractLotHighlight> saveLotHighlights(ContractLot contractLot, List<ContractLotHighlight> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return new ArrayList<>();
        }

        List<ContractLotHighlight> lotHighlights = highlights.stream().map(highlight -> ContractLotHighlight.builder().contractLot(contractLot).category(highlight.getCategory()).title(highlight.getTitle()).description(highlight.getDescription()).pageNumber(highlight.getPageNumber()).riskLevel(highlight.getRiskLevel()).confidenceScore(highlight.getConfidenceScore()).recommendedAction(highlight.getRecommendedAction()).bidCapable(highlight.getBidCapable()).build()).toList();

        List<ContractLotHighlight> savedHighlights = contractLotHighlightRepository.saveAll(lotHighlights);

        contractLot.getHighlights().clear();
        contractLot.getHighlights().addAll(savedHighlights);

        return savedHighlights;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private List<ContractLot> saveLots(ContractDocument contractDocument, List<ExtractedLotResponse> extractedLots) {

        List<ContractLot> savedLots = new ArrayList<>();

        for (ExtractedLotResponse extractedLot : extractedLots) {
            validateExtractedLot(extractedLot);

            ContractLot contractLot = ContractLot.builder().contractDocument(contractDocument).lotNumber(extractedLot.getLotNumber()).lotName(extractedLot.getLotName()).description(extractedLot.getDescription()).startPage(extractedLot.getStartPage()).endPage(extractedLot.getEndPage()).qualificationStatus(LotQualificationStatus.PENDING).createdAt(LocalDateTime.now()).valuation(extractedLot.getValuation()).build();

            savedLots.add(contractLotRepository.save(contractLot));
        }

        return savedLots;
    }

    private String buildContractText(List<ContractPageText> pages) {
        StringBuilder builder = new StringBuilder();

        for (ContractPageText page : pages) {
            builder.append("\n\n--- PAGE ").append(page.getPageNumber()).append(" ---\n");

            if (page.getText() != null && !page.getText().isBlank()) {
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
            return objectMapper.readValue(cleanedResponse, new TypeReference<List<ExtractedLotResponse>>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to parse lot extraction AI response: " + cleanedResponse, exception);
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
            cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
        }

        return cleanedResponse.trim();
    }

    private void validateExtractedLot(ExtractedLotResponse extractedLot) {
        if (extractedLot == null) {
            throw new IllegalStateException("Extracted lot must not be null");
        }

        if (extractedLot.getLotNumber() == null || extractedLot.getLotNumber().isBlank()) {
            throw new IllegalStateException("Extracted lot number must not be empty");
        }

        if (extractedLot.getLotName() == null || extractedLot.getLotName().isBlank()) {
            throw new IllegalStateException("Extracted lot name must not be empty for lot number: " + extractedLot.getLotNumber());
        }

        if (extractedLot.getStartPage() != null && extractedLot.getEndPage() != null && extractedLot.getStartPage() > extractedLot.getEndPage()) {
            throw new IllegalStateException("Lot start page cannot be greater than end page for lot number: " + extractedLot.getLotNumber());
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
