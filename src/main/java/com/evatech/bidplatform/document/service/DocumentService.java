package com.evatech.bidplatform.document.service;

import com.evatech.bidplatform.document.entity.GeneratedDocument;
import com.evatech.bidplatform.user.entity.User;
import org.springframework.core.io.Resource;

public interface DocumentService {

    GeneratedDocument generateBidDocument(Long bidId, User generatedBy);
    Resource downloadDocument(Long documentId, User user);
    GeneratedDocument getDocument(Long documentId, User user);
}
