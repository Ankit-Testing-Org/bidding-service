package com.evatech.bidplatform.document.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.document.entity.GeneratedDocument;
import com.evatech.bidplatform.document.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(
            DocumentService documentService
    ) {
        this.documentService = documentService;
    }

    @PostMapping("/bids/{bidId}/generate")
    public ApiResponse<GeneratedDocument> generateBidDocument(
            @PathVariable Long bidId,
            @RequestParam String generatedBy
    ) {
        GeneratedDocument generatedDocument =
                documentService.generateBidDocument(bidId, generatedBy);

        return ApiResponse.success(
                "Bid document generated successfully",
                generatedDocument
        );
    }

    @GetMapping("/{documentId}")
    public ApiResponse<GeneratedDocument> getDocument(
            @PathVariable Long documentId
    ) {
        GeneratedDocument generatedDocument =
                documentService.getDocument(documentId);

        return ApiResponse.success(
                "Generated document fetched successfully",
                generatedDocument
        );
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId
    ) {
        GeneratedDocument generatedDocument =
                documentService.getDocument(documentId);

        Resource resource =
                documentService.downloadDocument(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + generatedDocument.getFileName() + "\""
                )
                .body(resource);
    }
}