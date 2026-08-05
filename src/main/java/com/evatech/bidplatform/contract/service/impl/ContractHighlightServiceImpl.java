package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.ai.service.AiRequestLoggerService;
import com.evatech.bidplatform.ai.service.AiService;
import com.evatech.bidplatform.contract.entity.*;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import com.evatech.bidplatform.contract.repository.ContractHighlightRepository;
import com.evatech.bidplatform.contract.repository.ContractPageTextRepository;
import com.evatech.bidplatform.contract.service.ContractHighlightService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public List<ContractHighlight> analyseContract(
            Long contractId,
            boolean reanalyse,
            User user) {
        ContractDocument contractDocument = contractDocumentRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contract not found with id: " + contractId
                ));

        boolean alreadyAnalysed = contractHighlightRepository.existsByContractDocumentId(contractId);

        if (alreadyAnalysed && !reanalyse) {
            return contractHighlightRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId);
        }

        List<ContractPageText> pages = contractPageTextRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId);

        if (pages.isEmpty()) {
            throw new IllegalStateException("Contract text must be extracted before analysis");
        }
        contractService.markAnalysisInProgress(contractId);

        // START : Below is AI logic.
        if (reanalyse) {
            contractHighlightRepository.deleteByContractDocumentId(contractId);
        }

        // This method will be used to analyse contract.
        List<ContractHighlight> highlights = aiService.analyseContract(contractDocument, pages);
        aiRequestLoggerService.logRequest(contractId, contractDocument.getAssignedTo());

        contractHighlightRepository.deleteByContractDocumentId(contractId);

        List<ContractHighlight> contractHighlights = saveHighlights(contractId, highlights);
        // END : Below is AI logic.

        contractService.markAnalysed(contractId);
        return contractHighlights;
    }

    private List<ContractHighlight> saveHighlights(Long contractId, List<ContractHighlight> highlights) {
        ContractDocument contractDocument = contractDocumentRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contract not found with id: " + contractId
                ));

        contractHighlightRepository.deleteByContractDocumentId(contractId);

        for (ContractHighlight highlight : highlights) {
            highlight.setContractDocument(contractDocument);
            contractHighlightRepository.save(highlight);
        }

        contractDocument.setStatus(ContractStatus.ANALYSED);
        contractDocumentRepository.save(contractDocument);

        return contractHighlightRepository
                .findByContractDocumentIdOrderByPageNumberAsc(contractId);
    }
}