package com.evatech.bidplatform.document.service;


import com.evatech.bidplatform.document.entity.DocumentTemplate;
import com.evatech.bidplatform.document.entity.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentTemplateService {

    DocumentTemplate uploadTemplate(
            MultipartFile file,
            String templateName,
            String templateCode,
            DocumentType documentType,
            String uploadedBy
    );

    DocumentTemplate getActiveTemplate(String templateCode);

    List<DocumentTemplate> getTemplatesByType(DocumentType documentType);

    DocumentTemplate deactivateTemplate(String templateCode, String updatedBy);

    DocumentTemplate getTemplate(Long templateId);

    List<DocumentTemplate> getActiveTemplatesByType(DocumentType documentType);
}
