package com.evatech.bidplatform.document.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.document.entity.DocumentTemplate;
import com.evatech.bidplatform.document.entity.DocumentType;
import com.evatech.bidplatform.document.service.DocumentTemplateService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/document-templates")
@RequiredArgsConstructor
public class DocumentTemplateController extends AbstractController {

    private final DocumentTemplateService documentTemplateService;
    private final UserRepository userRepo;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<DocumentTemplate> uploadTemplate(
            Authentication authentication,
            @RequestPart("file") MultipartFile file,
            @RequestParam String templateName,
            @RequestParam String templateCode,
            @RequestParam DocumentType documentType,
            @RequestParam String uploadedBy
    ) {
        authenticateAndFetchUser(userRepo,authentication);
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
            Authentication authentication,
            @PathVariable String templateCode
    ) {
        authenticateAndFetchUser(userRepo,authentication);
        DocumentTemplate template =
                documentTemplateService.getActiveTemplate(templateCode);

        return ApiResponse.success(
                "Document template fetched successfully",
                template
        );
    }

    @GetMapping
    public ApiResponse<List<DocumentTemplate>> getTemplatesByType(
            Authentication authentication,
            @RequestParam DocumentType documentType
    ) {
        authenticateAndFetchUser(userRepo,authentication);
        List<DocumentTemplate> templates =
                documentTemplateService.getTemplatesByType(documentType);

        return ApiResponse.success(
                "Document templates fetched successfully",
                templates
        );
    }

    @PostMapping("/{templateCode}/deactivate")
    public ApiResponse<DocumentTemplate> deactivateTemplate(
            Authentication authentication,
            @PathVariable String templateCode,
            @RequestParam String updatedBy
    ) {
        authenticateAndFetchUser(userRepo,authentication);
        DocumentTemplate template =
                documentTemplateService.deactivateTemplate(templateCode, updatedBy);

        return ApiResponse.success(
                "Document template deactivated successfully",
                template
        );
    }
}