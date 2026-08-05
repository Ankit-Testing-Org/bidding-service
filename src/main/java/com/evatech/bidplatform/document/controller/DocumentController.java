package com.evatech.bidplatform.document.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.document.entity.GeneratedDocument;
import com.evatech.bidplatform.document.service.DocumentService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController extends AbstractController {

    private final DocumentService documentService;
    private final UserRepository userRepo;

    @PostMapping("/bids/{bidId}/generate")
    public ApiResponse<GeneratedDocument> generateBidDocument(
            Authentication authentication,
            @PathVariable Long bidId,
            @RequestParam String generatedBy
    ) {
        authenticateAndFetchUser(userRepo,authentication);
        GeneratedDocument generatedDocument =
                documentService.generateBidDocument(bidId, generatedBy);

        return ApiResponse.success(
                "Bid document generated successfully",
                generatedDocument
        );
    }

    @GetMapping("/{documentId}")
    public ApiResponse<GeneratedDocument> getDocument(
            Authentication authentication,
            @PathVariable Long documentId
    ) {
        authenticateAndFetchUser(userRepo,authentication);
        GeneratedDocument generatedDocument =
                documentService.getDocument(documentId);

        return ApiResponse.success(
                "Generated document fetched successfully",
                generatedDocument
        );
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            Authentication authentication,
            @PathVariable Long documentId
    ) {
        authenticateAndFetchUser(userRepo,authentication);
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