package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.bid.service.FileStorageService;
import com.evatech.bidplatform.contract.service.PdfPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfPageServiceImpl implements PdfPageService {

    private final FileStorageService fileStorageService;

    @Override
    public Resource extractSinglePage(
            Path pdfPath,
            Long contractId,
            Integer pageNumber
    ) {
        validatePageNumber(pageNumber);

        try (PDDocument sourceDocument = Loader.loadPDF(pdfPath.toFile())) {
            int totalPages = sourceDocument.getNumberOfPages();

            if (pageNumber > totalPages) {
                throw new IllegalArgumentException(
                        "Requested page " + pageNumber + " exceeds total pages " + totalPages
                );
            }

            Splitter splitter = new Splitter();
            splitter.setStartPage(pageNumber);
            splitter.setEndPage(pageNumber);
            splitter.setSplitAtPage(1);

            List<PDDocument> splitPages = splitter.split(sourceDocument);

            if (splitPages.isEmpty()) {
                throw new IllegalStateException("Could not extract page: " + pageNumber);
            }

            Path tempDirectory = fileStorageService.getTempDirectory();

            Path tempFile = Files.createTempFile(
                    tempDirectory,
                    "contract-" + contractId + "-page-" + pageNumber + "-",
                    ".pdf"
            );

            try (PDDocument singlePageDocument = splitPages.get(0)) {
                singlePageDocument.save(tempFile.toFile());
            }

            return new FileSystemResource(tempFile);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to extract PDF page", exception);
        }
    }

    @Override
    public void validatePageNumber(Integer pageNumber) {
        if (pageNumber == null || pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be greater than zero");
        }
    }
}
