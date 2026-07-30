package com.evatech.bidplatform.contract.controller;


import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractHighlight;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.service.ContractHighlightService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.contract.service.ContractTextExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final ContractTextExtractionService contractTextExtractionService;
    private final ContractHighlightService contractHighlightService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<ContractDocument> uploadContract(
            @RequestPart("file") MultipartFile file,
            @RequestParam("uploadedBy") String uploadedBy
    ) {
        ContractDocument contractDocument = contractService.uploadContract(file, uploadedBy);

        return ApiResponse.success(
                "Contract uploaded successfully",
                contractDocument
        );
    }

    @GetMapping("/{contractId}")
    public ApiResponse<ContractDocument> getContract(
            @PathVariable Long contractId
    ) {
        ContractDocument contractDocument = contractService.getContract(contractId);

        return ApiResponse.success(
                "Contract fetched successfully",
                contractDocument
        );
    }

    @PostMapping("/{contractId}/extract-text")
    public ApiResponse<List<ContractPageText>> extractText(
            @PathVariable Long contractId
    ) {
        List<ContractPageText> pages =
                contractTextExtractionService.extractText(contractId);

        return ApiResponse.success(
                "Contract text extracted successfully",
                pages
        );
    }

    @GetMapping("/{contractId}/pages")
    public ApiResponse<List<ContractPageText>> getContractPages(
            @PathVariable Long contractId,
            @RequestParam(required = false) Integer pageNumber
    ) {
        List<ContractPageText> pages = contractService.getContractPages(contractId, pageNumber);

        return ApiResponse.success(
                "Contract pages fetched successfully",
                pages
        );
    }

    @PostMapping("/{contractId}/analyse")
    public ApiResponse<List<ContractHighlight>> analyseContract(
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "false") boolean reanalyse
    ) {
        List<ContractHighlight> highlights =
                contractHighlightService.analyseContract(contractId,
                        reanalyse);
        return ApiResponse.success("Contract analysis processed successfully", highlights);
    }

    @GetMapping("/{contractId}/highlights")
    public ApiResponse<List<ContractHighlight>> getHighlights(
            @PathVariable Long contractId
    ) {
        List<ContractHighlight> highlights = contractService.getHighlights(contractId);

        return ApiResponse.success(
                "Contract highlights fetched successfully",
                highlights
        );
    }
}
