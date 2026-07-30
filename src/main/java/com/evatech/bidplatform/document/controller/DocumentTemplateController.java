package com.evatech.bidplatform.document.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.document.entity.DocumentTemplate;
import com.evatech.bidplatform.document.entity.DocumentType;
import com.evatech.bidplatform.document.service.DocumentTemplateService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/document-templates")
public class DocumentTemplateController {

    private final DocumentTemplateService documentTemplateService;

    public DocumentTemplateController(
            DocumentTemplateService documentTemplateService
    ) {
        this.documentTemplateService = documentTemplateService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<DocumentTemplate> uploadTemplate(
            @RequestPart("file") MultipartFile file,
            @RequestParam String templateName,
            @RequestParam String templateCode,
            @RequestParam DocumentType documentType,
            @RequestParam String uploadedBy
    ) {
        DocumentTemplate template = documentTemplateService.uploadTemplate(
                file,
                templateName,
                templateCode,
                documentType,
                uploadedBy
        );

        return ApiResponse.success(
                "Document template uploaded successfully",
                template
        );
    }

    @GetMapping("/{templateCode}")
    public ApiResponse<DocumentTemplate> getActiveTemplate(
            @PathVariable String templateCode
    ) {
        DocumentTemplate template =
                documentTemplateService.getActiveTemplate(templateCode);

        return ApiResponse.success(
                "Document template fetched successfully",
                template
        );
    }

    @GetMapping
    public ApiResponse<List<DocumentTemplate>> getTemplatesByType(
            @RequestParam DocumentType documentType
    ) {
        List<DocumentTemplate> templates =
                documentTemplateService.getTemplatesByType(documentType);

        return ApiResponse.success(
                "Document templates fetched successfully",
                templates
        );
    }

    @PostMapping("/{templateCode}/deactivate")
    public ApiResponse<DocumentTemplate> deactivateTemplate(
            @PathVariable String templateCode,
            @RequestParam String updatedBy
    ) {
        DocumentTemplate template =
                documentTemplateService.deactivateTemplate(templateCode, updatedBy);

        return ApiResponse.success(
                "Document template deactivated successfully",
                template
        );
    }
}