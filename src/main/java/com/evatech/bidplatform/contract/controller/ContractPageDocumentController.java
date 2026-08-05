package com.evatech.bidplatform.contract.controller;

import com.evatech.bidplatform.contract.service.ContractPageDocumentService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractPageDocumentController extends AbstractController {

    private final ContractPageDocumentService contractPageDocumentService;
    private final UserRepository userRepo;

    @GetMapping("/{contractId}/pages/{pageNumber}/document")
    public ResponseEntity<Resource> getContractPageAsDocument(
            Authentication authentication,
            @PathVariable Long contractId,
            @PathVariable Integer pageNumber
    ) {
        authenticateAndFetchUser(userRepo,authentication);
        Resource resource = contractPageDocumentService.getContractPageAsDocument(contractId, pageNumber);

        String fileName = contractPageDocumentService.getGeneratedFileName(
                contractId, pageNumber
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
