package com.evatech.bidplatform.bid.controller;
import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.bid.dto.response.GeneratedDocumentResponse;
import com.evatech.bidplatform.bid.mapper.GeneratedDocumentMapper;
import com.evatech.bidplatform.bid.service.BidTemplateService;
import com.evatech.bidplatform.contract.controller.AbstractController;
import com.evatech.bidplatform.bid.entity.GeneratedDocument;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bid")
@RequiredArgsConstructor
public class BidTemplateController extends AbstractController {

    private final UserRepository userRepository;
    private final BidTemplateService bidTemplateService;
    private final GeneratedDocumentMapper generatedDocumentMapper;

    @PostMapping("/{contractId}/generate-template")
    public ApiResponse<GeneratedDocumentResponse> generateBidTemplate(
            Authentication authentication,
            @PathVariable Long contractId) {

        User user = authenticateAndFetchUser(userRepository,
                authentication);
        List<String> roles = fetchRolesForUser(authentication);

        GeneratedDocument template =
                bidTemplateService.generateBidTemplate(
                        contractId,
                        user,
                        roles);

        return ApiResponse.success(
                "Bid template generated successfully",
                generatedDocumentMapper.toResponse(template));
    }

    @GetMapping("/contracts/{contractId}/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            Authentication authentication,
            @PathVariable Long contractId,
            @PathVariable Long documentId) {
        User user = authenticateAndFetchUser(userRepository,
                authentication);
        List<String> roles = fetchRolesForUser(authentication);

        Resource resource =bidTemplateService.downloadDocument(documentId,
                contractId, user, roles);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
