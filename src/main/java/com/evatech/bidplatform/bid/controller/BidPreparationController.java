package com.evatech.bidplatform.bid.controller;


import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.bid.dto.request.GenerateBidDocumentRequest;
import com.evatech.bidplatform.bid.dto.request.SubmitBidForApprovalRequest;
import com.evatech.bidplatform.bid.dto.response.*;
import com.evatech.bidplatform.bid.entity.BidDocumentType;
import com.evatech.bidplatform.contract.controller.AbstractController;
import com.evatech.bidplatform.dashboard.service.BidPreparationService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bid/contracts/{contractId}")
@RequiredArgsConstructor
public class BidPreparationController extends AbstractController {

    private final UserRepository userRepository;
    private final BidPreparationService bidPreparationService;

    @GetMapping("/documents/latest")
    public ApiResponse<LatestBidDocumentResponse> getLatestBidDocument(
            Authentication authentication,
            @PathVariable Long contractId) {

        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        List<String> roles = fetchRolesForUser(authentication);

        LatestBidDocumentResponse response =
                bidPreparationService.getLatestBidDocument(
                        contractId,
                        user,
                        roles
                );

        String message = response.documentExists()
                ? "Latest bid document retrieved successfully"
                : "No generated bid document found";

        return ApiResponse.success(message, response);
    }

    @PostMapping("/documents/generate")
    public ApiResponse<GeneratedBidDocumentResponse> generateBidDocument(
            Authentication authentication,
            @PathVariable Long contractId,
            @Valid @RequestBody GenerateBidDocumentRequest request) {

        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        List<String> roles = fetchRolesForUser(authentication);

        GeneratedBidDocumentResponse response =
                bidPreparationService.generateBidDocument(
                        contractId,
                        request,
                        user,
                        roles
                );

        return ApiResponse.success(
                "Bid document generated successfully",
                response
        );
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadBidDocument(
            Authentication authentication,
            @PathVariable Long contractId,
            @PathVariable Long documentId) {

        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        List<String> roles = fetchRolesForUser(authentication);

        Resource resource = bidPreparationService.downloadDocument(
                contractId,
                documentId,
                user,
                roles
        );

        String fileName = resource.getFilename() != null
                ? resource.getFilename()
                : "bid-document";

        ContentDisposition contentDisposition =
                ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/documents/{documentId}/preview")
    public ResponseEntity<Resource> previewBidDocument(
            Authentication authentication,
            @PathVariable Long contractId,
            @PathVariable Long documentId) {

        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        List<String> roles = fetchRolesForUser(authentication);

        BidDocumentDetailsResponse document =
                bidPreparationService.getDocumentDetails(
                        contractId,
                        documentId,
                        user,
                        roles
                );

        Resource resource = bidPreparationService.previewDocument(
                contractId,
                documentId,
                user,
                roles
        );

        MediaType mediaType = resolveMediaType(
                document.contentType()
        );

        String fileName = resource.getFilename() != null
                ? resource.getFilename()
                : document.fileName();

        ContentDisposition contentDisposition =
                ContentDisposition.inline()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .contentType(mediaType)
                .body(resource);
    }

    @PostMapping(
            value = "/documents/upload-completed",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<UploadedBidDocumentResponse> uploadCompletedBid(
            Authentication authentication,
            @PathVariable Long contractId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(
                    value = "sourceGeneratedDocumentId",
                    required = false
            )
            Long sourceGeneratedDocumentId,
            @RequestParam(
                    value = "documentType",
                    defaultValue = "PREPARED_BID_DOCUMENT"
            )
            BidDocumentType documentType) {

        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        List<String> roles = fetchRolesForUser(authentication);

        UploadedBidDocumentResponse response =
                bidPreparationService.uploadCompletedBid(
                        contractId,
                        sourceGeneratedDocumentId,
                        documentType,
                        file,
                        user,
                        roles
                );

        return ApiResponse.success(
                "Completed bid uploaded successfully",
                response
        );
    }

    @PostMapping("/submit-for-approval")
    public ApiResponse<SubmitBidForApprovalResponse> submitForApproval(
            Authentication authentication,
            @PathVariable Long contractId,
            @Valid @RequestBody SubmitBidForApprovalRequest request) {

        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        List<String> roles = fetchRolesForUser(authentication);

        SubmitBidForApprovalResponse response =
                bidPreparationService.submitForApproval(
                        contractId,
                        request,
                        user,
                        roles
                );

        return ApiResponse.success(
                "Bid submitted for approval successfully",
                response
        );
    }

    @GetMapping("/preparation-status")
    public ApiResponse<BidPreparationStatusResponse> getPreparationStatus(
            Authentication authentication,
            @PathVariable Long contractId) {

        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        List<String> roles = fetchRolesForUser(authentication);

        BidPreparationStatusResponse response =
                bidPreparationService.getPreparationStatus(
                        contractId,
                        user,
                        roles
                );

        return ApiResponse.success(
                "Bid preparation status retrieved successfully",
                response
        );
    }

    private MediaType resolveMediaType(String contentType) {

        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "Unsupported content type received: {}",
                    contentType
            );

            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}