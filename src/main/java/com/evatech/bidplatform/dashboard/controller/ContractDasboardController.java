package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.service.ContractLotService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.dashboard.dto.contract.request.AssignContractRequest;
import com.evatech.bidplatform.dashboard.dto.contract.request.ContractSearchRequest;
import com.evatech.bidplatform.dashboard.dto.contract.request.UnassignContractRequest;
import com.evatech.bidplatform.dashboard.dto.contract.response.*;
import com.evatech.bidplatform.dashboard.mapper.contract.*;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractDasboardController extends AbstractController {

    private final ContractService contractService;
    private final UserRepository userRepository;
    private final ContractLotService contractLotService;

    private final ContractDashboardMapper contractDashboardMapper;
    private final ContractDetailsMapper contractDetailsMapper;
    private final ContractLotMapper contractLotMapper;
    private final ContractAnalysisAccessMapper contractAnalysisAccessMapper;
    private final ContractAssignmentMapper contractAssignmentMapper;

    @GetMapping("/dashboard")
    public ApiResponse<ContractsDashboardResponse> getContractDashboard(
            Authentication authentication
    ) {
        User user = authenticateAndFetchUser(userRepository, authentication);

        List<ContractDocument> contractDocuments = contractService.getContractDashboard();
        if(!contractDocuments.isEmpty()) {
            ContractSummaryResponse summary =
                    contractDashboardMapper.toSummary(contractDocuments, user.getEmail());
            List<ContractListItemResponse> contracts =
                    contractDashboardMapper.toListItems(contractDocuments, user.getEmail());
            ApiResponse.success("Contract returned successfully",
                    new ContractsDashboardResponse(summary, contracts));
        }
        return ApiResponse.failure("Failed to return contract");
    }

    @GetMapping("/{contractId}")
    public ApiResponse<ContractDetailsResponse> getContractById(
            Authentication authentication,
            @PathVariable("contractId") Long contractId
    ) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contractDocument =
                contractService.getContract(contractId, user, roles);
        if(contractDocument == null) {
            return ApiResponse.failure("Contract not found");
        }
        return  ApiResponse.success("Contract returned successfully",
                contractDetailsMapper.toResponse(contractDocument));
    }

    @GetMapping("/{contractId}/lots")
    public ApiResponse<List<ContractLotResponse>> getContractLotsByContractId(
            Authentication authentication,
            @PathVariable("contractId") Long contractId
    ) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        List<ContractLot> contractLots = contractLotService.
                getContractLots(contractId, user, roles);

        if(contractLots.isEmpty()) {
            return ApiResponse.failure("Contract Lots not found");
        }
        return  ApiResponse.success("Contract lots returned successfully",
                contractLotMapper.toResponseList(contractLots));
    }

    @GetMapping("/{contractId}/analysis-access")
    public ApiResponse<ContractAnalysisAccessResponse> getContractAnalysisAccessById(
            Authentication authentication,
            @PathVariable("contractId") Long contractId
    ) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contractDocument =
                contractService.getContract(contractId, user, roles);

        if(contractDocument == null) {
            return ApiResponse.failure("Failed with contract analysis access");
        }

        return ApiResponse.success(
                "Analysis access checked successfully",
                contractAnalysisAccessMapper.toResponse(contractDocument, user.getEmail())
        );
    }


    @PostMapping("/{contractId}/assign")
    public ApiResponse<AssignContractResponse> getContractByIdAssign(
            Authentication authentication,
            @PathVariable("contractId") Long contractId,
            @RequestBody AssignContractRequest assignContractRequest
            ) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contractDocument = contractService.
                assignContract(contractId, assignContractRequest.assignedTo(),
                user, roles, assignContractRequest.comment());
        if(contractDocument == null) {
            return ApiResponse.failure("Failed with contract analysis access");
        }

        return ApiResponse.success(
                "Analysis access checked successfully",
                contractAssignmentMapper.toAssignResponse(contractDocument)
        );
    }


    @PostMapping("/{contractId}/unassign")
    public ApiResponse<UnassignContractResponse> getContractByIdunassign(
            Authentication authentication,
            @PathVariable("contractId") Long contractId,
            @RequestBody UnassignContractRequest unassignContractRequest
    ) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contractDocument = contractService.
                unassignContract(contractId, user, roles,
                        unassignContractRequest.comment());
        if(contractDocument == null) {
            return ApiResponse.failure("Failed with contract analysis access");
        }
        return ApiResponse.success(
                "Analysis access checked successfully",
                contractAssignmentMapper.toUnassignResponse(contractDocument)
        );
    }

    @PostMapping("/search")
    public ApiResponse<ContractSearchResponse> getContractsSearch(
            Authentication authentication,
            @RequestBody ContractSearchRequest contractSearchRequest
            ) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        Page<ContractDocument> page = contractService.searchContracts(user,
                roles, contractSearchRequest);
        if(page == null) {
            return ApiResponse.failure("Failed to get contract with search");
        }
        ContractSearchResponse response =
                new ContractSearchResponse(
                        contractDashboardMapper.toListItems(
                                page.getContent(),
                                user.getEmail()),
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast());

        return ApiResponse.success(
                "Contracts retrieved successfully",
                response
        );
    }

    @GetMapping("/{contractId}/pdf")
    public ResponseEntity<Resource> getContractByIdPdf(
            Authentication authentication,
            @PathVariable("contractId") Long contractId
    ) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        Resource resource = contractService.retrieveContractPdf(contractId,
                user, roles);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"").body(resource);
    }
}
