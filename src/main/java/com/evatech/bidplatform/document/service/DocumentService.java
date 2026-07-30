package com.evatech.bidplatform.document.service;

import com.evatech.bidplatform.document.entity.GeneratedDocument;
import org.springframework.core.io.Resource;

public interface DocumentService {

    GeneratedDocument generateBidDocument(Long bidId, String generatedBy);
    Resource downloadDocument(Long documentId);
    GeneratedDocument getDocument(Long documentId);
}
