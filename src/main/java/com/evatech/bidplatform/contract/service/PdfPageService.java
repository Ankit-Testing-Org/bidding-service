package com.evatech.bidplatform.contract.service;

import org.springframework.core.io.Resource;

import java.nio.file.Path;

public interface PdfPageService {

    Resource extractSinglePage(
            Path pdfPath,
            Long contractId,
            Integer pageNumber
    );

    void validatePageNumber(Integer pageNumber);
}
