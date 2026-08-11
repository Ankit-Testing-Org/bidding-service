package com.evatech.bidplatform.bid.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.bid.dto.request.AssignBidRequest;
import com.evatech.bidplatform.bid.dto.response.BidDocumentResponse;
import com.evatech.bidplatform.bid.dto.response.BidResponse;
import com.evatech.bidplatform.bid.dto.response.BidTemplateFieldUpdateResponse;
import com.evatech.bidplatform.bid.dto.response.DeleteDocumentResponse;
import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidDocument;
import com.evatech.bidplatform.bid.entity.BidDocumentType;
import com.evatech.bidplatform.bid.mapper.BidDocumentMapper;
import com.evatech.bidplatform.bid.mapper.BidMapper;
import com.evatech.bidplatform.bid.service.BidService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bid")
@RequiredArgsConstructor
public class BidController extends AbstractController {

    private final BidService bidService;
    private final UserRepository userRepository;
    private final BidMapper bidMapper;
    private final BidDocumentMapper bidDocumentMapper;

    @PostMapping("/{contractId}/assign")
    public ApiResponse<BidResponse> assignBid(Authentication authentication,
                                              @PathVariable Long contractId,
                                              @Valid @RequestBody AssignBidRequest request) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        Bid bid = bidService.assignBid(contractId, request, user, roles);
        return ApiResponse.success("Bid assigned successfully", bidMapper.toResponse(bid));
    }

    @PostMapping("/{bidId}/upload-documents")
    public ApiResponse<BidDocumentResponse> uploadDocument(Authentication authentication,
                                                           @PathVariable Long bidId,
                                                           @RequestParam("file") MultipartFile file,
                                                           @RequestParam("documentType") BidDocumentType documentType) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        BidDocument document = bidService.uploadDocument(bidId, file, documentType, user);
        return ApiResponse.success("Document uploaded successfully", bidDocumentMapper.toResponse(document));
    }

    @PostMapping("/{bidId}/template/{templateId}/update-contract")
    public ApiResponse<List<BidTemplateFieldUpdateResponse>> updateTemplateToContract(Authentication authentication,
                                                           @PathVariable Long bidId,
                                                           @PathVariable Long bidDocumentId,
                                                           @RequestParam("contractId") Long contractId) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        List<BidTemplateFieldUpdateResponse> responses = bidService.uploadTemplateToContract(bidId, bidDocumentId, contractId, user, roles);
        return ApiResponse.success("Document uploaded successfully", responses);
    }

    @PostMapping("/{bidId}/upload-completed-bid")
    public ApiResponse<BidDocumentResponse> uploadCompletedBid(Authentication authentication,
                                                               @PathVariable Long bidId,
                                                               @RequestParam("file") MultipartFile file) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        BidDocument document = bidService.uploadCompletedBid(bidId, file, user, roles);
        return ApiResponse.success("Completed bid uploaded successfully", bidDocumentMapper.toResponse(document));
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadGeneratedDocument(Authentication authentication,
                                                              @PathVariable Long documentId) {

        User user = authenticateAndFetchUser(userRepository, authentication);

        List<String> roles = fetchRolesForUser(authentication);
        Resource resource = bidService.downloadDocument(documentId, user, roles);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @DeleteMapping("/{documentId}/delete")
    public ApiResponse<DeleteDocumentResponse> deleteGeneratedDocument(Authentication authentication,
                                                     @PathVariable Long documentId) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        DeleteDocumentResponse response = bidService.deleteDocument(documentId, user, roles);
        return ApiResponse.success("Document deleted successfully", response);
    }
}