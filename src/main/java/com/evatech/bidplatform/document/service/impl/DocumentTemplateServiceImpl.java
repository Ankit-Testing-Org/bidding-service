package com.evatech.bidplatform.document.service.impl;

import com.evatech.bidplatform.document.entity.DocumentTemplate;
import com.evatech.bidplatform.document.entity.DocumentType;
import com.evatech.bidplatform.document.repository.DocumentTemplateRepository;
import com.evatech.bidplatform.document.service.DocumentTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentTemplateServiceImpl implements DocumentTemplateService {

    private final DocumentTemplateRepository documentTemplateRepository;


    @Override
    public DocumentTemplate uploadTemplate(
            MultipartFile file,
            String templateName,
            String templateCode,
            DocumentType documentType,
            String uploadedBy
    ) {
        validateUploadRequest(
                file,
                templateName,
                templateCode,
                documentType,
                uploadedBy
        );

        if (documentTemplateRepository.existsByTemplateCode(templateCode)) {
            throw new IllegalArgumentException(
                    "Document template already exists with code: " + templateCode
            );
        }

        String originalFileName = file.getOriginalFilename();
        String storagePath = buildStoragePath(templateCode, originalFileName);

        DocumentTemplate documentTemplate = DocumentTemplate.builder()
                .templateName(templateName)
                .templateCode(templateCode)
                .documentType(documentType)
                .fileName(originalFileName)
                .storagePath(storagePath)
                .active(true)
                .uploadedBy(uploadedBy)
                .build();

        return documentTemplateRepository.save(documentTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentTemplate getTemplate(Long templateId) {
        return documentTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document template not found with id: " + templateId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentTemplate getActiveTemplate(String templateCode) {
        validateTemplateCode(templateCode);

        return documentTemplateRepository.findByTemplateCodeAndActiveTrue(templateCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active document template not found with code: " + templateCode
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTemplate> getTemplatesByType(DocumentType documentType) {
        if (documentType == null) {
            throw new IllegalArgumentException("Document type must not be null");
        }

        return documentTemplateRepository.findByDocumentType(documentType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTemplate> getActiveTemplatesByType(DocumentType documentType) {
        if (documentType == null) {
            throw new IllegalArgumentException("Document type must not be null");
        }

        return documentTemplateRepository.findByDocumentTypeAndActiveTrue(documentType);
    }

    @Override
    public DocumentTemplate deactivateTemplate(String templateCode, String updatedBy) {
        validateTemplateCode(templateCode);

        if (updatedBy == null || updatedBy.isBlank()) {
            throw new IllegalArgumentException("Updated by must not be empty");
        }

        DocumentTemplate documentTemplate = documentTemplateRepository
                .findByTemplateCodeAndActiveTrue(templateCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active document template not found with code: " + templateCode
                ));

        documentTemplate.deactivate(updatedBy);

        return documentTemplateRepository.save(documentTemplate);
    }

    private void validateUploadRequest(
            MultipartFile file,
            String templateName,
            String templateCode,
            DocumentType documentType,
            String uploadedBy
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Template file must not be empty");
        }

        if (templateName == null || templateName.isBlank()) {
            throw new IllegalArgumentException("Template name must not be empty");
        }

        validateTemplateCode(templateCode);

        if (documentType == null) {
            throw new IllegalArgumentException("Document type must not be null");
        }

        if (uploadedBy == null || uploadedBy.isBlank()) {
            throw new IllegalArgumentException("Uploaded by must not be empty");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Template file name must not be empty");
        }

        if (!isSupportedTemplateFile(originalFileName)) {
            throw new IllegalArgumentException(
                    "Only DOCX and PDF template files are supported"
            );
        }
    }

    private void validateTemplateCode(String templateCode) {
        if (templateCode == null || templateCode.isBlank()) {
            throw new IllegalArgumentException("Template code must not be empty");
        }
    }

    private boolean isSupportedTemplateFile(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();

        return lowerCaseFileName.endsWith(".docx")
                || lowerCaseFileName.endsWith(".pdf");
    }

    private String buildStoragePath(String templateCode, String originalFileName) {
        return "templates/"
                + templateCode
                + "/"
                + System.currentTimeMillis()
                + "_"
                + originalFileName;
    }
}