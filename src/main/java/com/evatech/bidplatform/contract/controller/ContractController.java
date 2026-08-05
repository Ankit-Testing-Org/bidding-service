package com.evatech.bidplatform.contract.controller;


import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.dto.response.ContractLotResponse;
import com.evatech.bidplatform.contract.dto.response.ContractPageTextResponse;
import com.evatech.bidplatform.contract.dto.response.ContractResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractHighlight;
import com.evatech.bidplatform.contract.entity.ContractLot;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.mapper.ContractLotMapper;
import com.evatech.bidplatform.contract.mapper.ContractMapper;
import com.evatech.bidplatform.contract.mapper.ContractPageTextMapper;
import com.evatech.bidplatform.contract.service.ContractHighlightService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.contract.service.ContractTextExtractionService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController extends AbstractController {

    private final ContractService contractService;
    private final ContractTextExtractionService contractTextExtractionService;
    private final ContractHighlightService contractHighlightService;
    private final UserRepository userRepo;
    private final ContractMapper contractMapper;
    private final ContractPageTextMapper contractPageTextMapper;
    private final ContractLotMapper contractLotMapper;

    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ContractDocument> uploadContract(
            Authentication authentication,
            @RequestPart("file") MultipartFile file) {
        User user = authenticateAndFetchUser(userRepo,authentication);
        ContractDocument contractDocument = contractService.uploadContract(file, user);

        return ApiResponse.success(
                "Contract uploaded successfully",
                contractDocument
        );
    }

    @GetMapping("/{contractId}/fetch/contract")
    public ApiResponse<ContractResponse> getContract(
            Authentication authentication,
            @PathVariable Long contractId
    ) {
        User user = authenticateAndFetchUser(userRepo,authentication);
        ContractDocument contractDocument = contractService.getContract(contractId, user);

        return ApiResponse.success("Contract fetched successfully", contractMapper.toResponse(contractDocument));
    }

    @GetMapping("/{contractId}/fetch/contract/pages")
    public ApiResponse<List<ContractPageTextResponse>> getContractPages(
            Authentication authentication,
            @PathVariable Long contractId,
            @RequestParam(required = false) Integer pageNumber
    ) {
        User user = authenticateAndFetchUser(userRepo,authentication);
        List<ContractPageText> pages = contractService.getContractPages(contractId, pageNumber,
                user);
        return ApiResponse.success("Contract pages fetched successfully", contractPageTextMapper.toResponses(pages));
    }

    @GetMapping("/{contractId}/fetch/contract/lots")
    public ApiResponse<List<ContractLotResponse>> getContractLots(
            Authentication authentication,
            @PathVariable Long contractId,
            @RequestParam(required = false) String lotNumber
    ) {
        User user = authenticateAndFetchUser(userRepo,authentication);
        List<ContractLot> pages = contractService.getContractLots(contractId, user, lotNumber);
        return ApiResponse.success("Contract pages fetched successfully", contractLotMapper.toResponses(pages));
    }

    @PostMapping("/{contractId}/analyse/contract")
    public ApiResponse<List<ContractHighlight>> analyseContract(
            Authentication authentication,
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "false") boolean reanalyse
    ) {
        User user = authenticateAndFetchUser(userRepo,authentication);
        List<ContractHighlight> highlights =
                contractHighlightService.analyseContract(contractId,
                        reanalyse, user);
        return ApiResponse.success("Contract analysis processed successfully", highlights);
    }

    @GetMapping("/{contractId}/fetch/contract/highlights")
    public ApiResponse<List<ContractHighlight>> getHighlights(
            Authentication authentication,
            @PathVariable Long contractId
    ) {
        User user = authenticateAndFetchUser(userRepo,authentication);
        List<ContractHighlight> highlights = contractService.getHighlights(contractId, user);

        return ApiResponse.success(
                "Contract highlights fetched successfully",
                highlights
        );
    }
}
