package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import com.evatech.bidplatform.contract.repository.ContractPageTextRepository;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.contract.service.ContractTextExtractionService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ContractTextExtractionServiceImpl
        implements ContractTextExtractionService {

    private final ContractDocumentRepository contractDocumentRepository;
    private final ContractPageTextRepository contractPageTextRepository;
    private final ContractService contractService;

    @Override
    public List<ContractPageText> extractText(
            Long contractId,
            User user) {

        ContractDocument contractDocument = contractService.getContract(contractId, user);

        // TODO : NEED TO HAVE LOGIC OF REEXTRACT.
        contractPageTextRepository.deleteByContractDocumentId(contractId);

        List<String> extractedPages = extractPagesFromFile(contractDocument);

        if (extractedPages.isEmpty()) {
            throw new IllegalStateException(
                    "No text could be extracted from contract: "
                            + contractDocument.getOriginalFileName()
            );
        }
        List<ContractPageText> pages = new ArrayList<>();
        int pageNumber = 1;
        for (String pageContent : extractedPages) {
            ContractPageText pageText =
                    ContractPageText.builder()
                            .contractDocument(contractDocument)
                            .pageNumber(pageNumber++)
                            .text(pageContent)
                            .build();
            pages.add(pageText);
        }

        contractPageTextRepository.saveAll(pages);
        contractDocument.setPageCount(extractedPages.size());
        contractDocument.setStatus(ContractStatus.TEXT_EXTRACTED);

        contractDocumentRepository.save(contractDocument);

        return contractPageTextRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId);
    }

    @Override
    public List<ContractPageText> getContractPages(ContractDocument contractId, Integer pageNumber, User user) {
        if (pageNumber == null) {
            return contractPageTextRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId.getId());
        }

        ContractPageText pageText = contractPageTextRepository
                .findByContractDocumentIdAndPageNumber(contractId.getId(), pageNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Page not found for contract id: "
                                + contractId
                                + " and page number: "
                                + pageNumber
                ));
        return List.of(pageText);
    }

    /**
     * TODO : LOGIC NEEDS TO BE ADDED
     */
    private List<String> extractPagesFromFile(
            ContractDocument contractDocument) {

        List<String> pages = new ArrayList<>();
        return pages;
    }
}