package com.evatech.bidplatform.contract.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.common.controller.AbstractController;
import com.evatech.bidplatform.contract.dto.request.contract.AssignContractRequest;
import com.evatech.bidplatform.contract.dto.request.contract.ContractSearchRequest;
import com.evatech.bidplatform.contract.dto.request.contract.UnassignContractRequest;
import com.evatech.bidplatform.contract.dto.response.contract.ContractsSummariesResponse;
import com.evatech.bidplatform.contract.dto.response.contract.*;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.mapper.ContractLotMapper;
import com.evatech.bidplatform.contract.service.ContractLotAccessService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.contract.dto.response.lot.ContractLotResponse;
import com.evatech.bidplatform.contract.mapper.ContractAnalysisAccessMapper;
import com.evatech.bidplatform.contract.mapper.ContractAssignmentMapper;
import com.evatech.bidplatform.contract.mapper.ContractDashboardMapper;
import com.evatech.bidplatform.contract.mapper.ContractDetailsMapper;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractDashboardController extends AbstractController {

    private final ContractService contractService;
    private final UserRepository userRepository;
    private final ContractLotAccessService contractLotAccessService;
    private final ContractDashboardMapper contractDashboardMapper;
    private final ContractDetailsMapper contractDetailsMapper;
    private final ContractLotMapper contractLotMapper;
    private final ContractAnalysisAccessMapper contractAnalysisAccessMapper;
    private final ContractAssignmentMapper contractAssignmentMapper;

    @GetMapping("/dashboard")
    public ApiResponse<ContractsDashboardResponse> getContractDashboard() {
        User user = authenticateAndFetchUser(userRepository);

        List<ContractDocument> contractDocuments = contractService.getContractDashboard();
        if(!contractDocuments.isEmpty()) {
            ContractsSummariesResponse summary =
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
            @PathVariable("contractId") Long contractId
    ) {

        User user = authenticateAndFetchUser(userRepository);
        List<String> roles = fetchRolesForUser();

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
            @PathVariable("contractId") Long contractId
    ) {
        User user = authenticateAndFetchUser(userRepository);
        List<String> roles = fetchRolesForUser();

        List<ContractLot> contractLots = contractLotAccessService.getContractLots(contractId, user, roles);

        if(contractLots.isEmpty()) {
            return ApiResponse.failure("Contract Lots not found");
        }
        return  ApiResponse.success("Contract lots returned successfully",
                contractLotMapper.toResponseList(contractLots));
    }

    @GetMapping("/{contractId}/analysis-access")
    public ApiResponse<ContractAnalysisAccessResponse> getContractAnalysisAccessById(
            @PathVariable("contractId") Long contractId
    ) {
        User user = authenticateAndFetchUser(userRepository);
        List<String> roles = fetchRolesForUser();

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
            @PathVariable("contractId") Long contractId,
            @RequestBody AssignContractRequest assignContractRequest
            ) {

        User user = authenticateAndFetchUser(userRepository);
        List<String> roles = fetchRolesForUser();

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
            @PathVariable("contractId") Long contractId,
            @RequestBody UnassignContractRequest unassignContractRequest
    ) {

        User user = authenticateAndFetchUser(userRepository);
        List<String> roles = fetchRolesForUser();

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
            @RequestBody ContractSearchRequest contractSearchRequest
            ) {

        User user = authenticateAndFetchUser(userRepository);
        List<String> roles = fetchRolesForUser();

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
            @PathVariable("contractId") Long contractId
    ) {

        User user = authenticateAndFetchUser(userRepository);
        List<String> roles = fetchRolesForUser();

        Resource resource = contractService.retrieveContractPdf(contractId,
                user, roles);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"").body(resource);
    }
}
