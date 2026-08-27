package com.evatech.bidplatform.contract.controller;


import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.common.controller.AbstractController;
import com.evatech.bidplatform.contract.dto.response.contract.ContractPageTextResponse;
import com.evatech.bidplatform.contract.dto.response.contract.ContractResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import com.evatech.bidplatform.contract.mapper.ContractMapper;
import com.evatech.bidplatform.contract.mapper.ContractPageTextMapper;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController extends AbstractController {

    private final ContractService contractService;
    private final UserRepository userRepo;
    private final ContractMapper contractMapper;
    private final ContractPageTextMapper contractPageTextMapper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ContractResponse> uploadContract(@RequestPart("file") MultipartFile file) {
        User user = authenticateAndFetchUser(userRepo);
        List<String> roles = fetchRolesForUser();
        ContractDocument contractDocument = contractService.uploadContract(file, user, roles);

        return ApiResponse.success("Contract uploaded successfully",
                contractMapper.toResponse(contractDocument));
    }

    @GetMapping("/{contractId}/fetch/contract")
    public ApiResponse<ContractResponse> getContract(@PathVariable Long contractId) {
        User user = authenticateAndFetchUser(userRepo);
        List<String> roles = fetchRolesForUser();
        ContractDocument contractDocument = contractService.getContract(contractId, user, roles);

        return ApiResponse.success("Contract fetched successfully",
                contractMapper.toResponse(contractDocument));
    }

    @GetMapping("/{contractId}/fetch/contract/pages")
    public ApiResponse<List<ContractPageTextResponse>> getContractPages(@PathVariable Long contractId,
                                                                        @RequestParam(required = false) Integer pageNumber) {
        User user = authenticateAndFetchUser(userRepo);
        List<String> roles = fetchRolesForUser();
        List<ContractPageText> pages = contractService.getContractPages(contractId, pageNumber, user, roles);
        return ApiResponse.success("Contract pages fetched successfully",
                contractPageTextMapper.toResponses(pages));
    }
}
