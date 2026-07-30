package com.evatech.bidplatform.contract.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.service.ContractService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractAssignmentController {

    private final ContractService contractService;

    public ContractAssignmentController(
            ContractService contractService
    ) {
        this.contractService = contractService;
    }

    @PostMapping("/{contractId}/assign-to-me")
    public ApiResponse<ContractDocument> assignToMe(
            @PathVariable Long contractId,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user not found");
        }

        ContractDocument contractDocument = contractService.assignContract(
                contractId,
                authentication.getName()
        );

        return ApiResponse.success(
                "Contract assigned to current user successfully",
                contractDocument
        );
    }

    @GetMapping("/unassigned")
    public ApiResponse<List<ContractDocument>> getUnassignedContracts() {
        List<ContractDocument> contracts = contractService.getUnassignedContracts();

        return ApiResponse.success(
                "Unassigned contracts fetched successfully",
                contracts
        );
    }

    @GetMapping("/assigned")
    public ApiResponse<List<ContractDocument>> getAssignedContracts(
            @RequestParam String assignedTo
    ) {
        List<ContractDocument> contracts = contractService.getAssignedContracts(
                assignedTo
        );

        return ApiResponse.success(
                "Assigned contracts fetched successfully",
                contracts
        );
    }

    @PostMapping("/{contractId}/assign")
    public ApiResponse<ContractDocument> assignContract(
            @PathVariable Long contractId,
            @RequestParam String assignedTo
    ) {
        ContractDocument contractDocument = contractService.assignContract(
                contractId,
                assignedTo
        );

        return ApiResponse.success(
                "Contract assigned successfully",
                contractDocument
        );
    }

    @PostMapping("/{contractId}/unassign")
    public ApiResponse<ContractDocument> unassignContract(
            @PathVariable Long contractId
    ) {
        ContractDocument contractDocument = contractService.unassignContract(
                contractId
        );

        return ApiResponse.success(
                "Contract unassigned successfully",
                contractDocument
        );
    }
}