package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import com.evatech.bidplatform.contract.repository.ContractPageTextRepository;
import com.evatech.bidplatform.contract.service.ContractTextExtractionService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ContractTextExtractionServiceImpl implements ContractTextExtractionService {

    private final ContractDocumentRepository contractDocumentRepository;
    private final ContractPageTextRepository contractPageTextRepository;


    @Override
    public List<ContractPageText> extractText(Long contractId, User user) {
        ContractDocument contractDocument = contractDocumentRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found with id: " + contractId));

        contractPageTextRepository.deleteByContractDocumentId(contractId);

        String extractedText = extractTextFromFile(contractDocument);

        ContractPageText pageText = ContractPageText.builder()
                .contractDocument(contractDocument)
                .pageNumber(1)
                .text(extractedText)
                .build();

        contractPageTextRepository.save(pageText);

        contractDocument.setStatus(ContractStatus.TEXT_EXTRACTED);
        contractDocumentRepository.save(contractDocument);

        return contractPageTextRepository.findByContractDocumentIdOrderByPageNumberAsc(contractId);
    }

    private String extractTextFromFile(ContractDocument contractDocument) {
        return "Text extraction placeholder for file: "
                + contractDocument.getOriginalFileName()
                + ". Replace this logic with PDFBox or DOCX extraction.";
    }
}