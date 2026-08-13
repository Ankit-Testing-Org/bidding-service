package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.bid.service.FileStorageService;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import com.evatech.bidplatform.contract.repository.ContractPageTextRepository;
import com.evatech.bidplatform.contract.service.ContractPageDocumentService;
import com.evatech.bidplatform.contract.service.PdfPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ContractPageDocumentServiceImpl implements ContractPageDocumentService {

    private final ContractDocumentRepository contractDocumentRepository;
    private final ContractPageTextRepository contractPageTextRepository;
    private final FileStorageService fileStorageService;
    private final PdfPageService pdfPageService;

    @Override
    public Resource getContractPageAsDocument(
            Long contractId,
            Integer pageNumber
    ) {
        pdfPageService.validatePageNumber(pageNumber);

        ContractDocument contractDocument = contractDocumentRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contract not found with id: " + contractId
                ));

        contractPageTextRepository.findByContractDocumentIdAndPageNumber(
                contractId,
                pageNumber
        ).orElseThrow(() -> new IllegalArgumentException(
                "Page not found for contract id: " + contractId + " and page number: " + pageNumber
        ));

        Path sourceFilePath = fileStorageService.resolvePath(
                contractDocument.getStoragePath()
        );

        if (!Files.exists(sourceFilePath)) {
            throw new IllegalStateException(
                    "Contract file does not exist at path: " + sourceFilePath
            );
        }

        //TODO: FOR NOW JUST PDF SUPPORT LATER DOCUMENT SUPPORT TOO.
        if (!isPdf(contractDocument)) {
            throw new IllegalStateException(
                    "Only PDF page document preview is supported for now"
            );
        }

      return pdfPageService.extractSinglePage(
                sourceFilePath,
                contractId,
                pageNumber
        );
    }

    @Override
    public String getGeneratedFileName(
            Long contractId,
            Integer pageNumber
    ) {
        return "contract-" +contractId + "-page-" + pageNumber + ".pdf";
    }

    private boolean isPdf(ContractDocument contractDocument) {
        String fileType = contractDocument.getFileType();
        String originalFileName = contractDocument.getOriginalFileName();

        if (fileType != null && fileType.equalsIgnoreCase("application/pdf")) {
            return true;
        }

        return originalFileName != null
                && originalFileName.toLowerCase().endsWith(".pdf");
    }
}
