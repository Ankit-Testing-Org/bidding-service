package com.evatech.bidplatform.contract.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.dto.response.contract.ContractResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.mapper.ContractMapper;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractAssignmentController extends AbstractController {

    private final ContractService contractService;
    private final UserRepository userRepo;
    private final ContractMapper contractMapper;

    @GetMapping("/unassigned")
    public ApiResponse<List<ContractResponse>> getUnassignedContracts(Authentication authentication) {
        User user = authenticateAndFetchUser(userRepo,authentication);
        List<String> roles = fetchRolesForUser(authentication);

        List<ContractDocument> contracts = contractService.getUnassignedContracts(user,roles);

        return ApiResponse.success(
                "Unassigned contracts fetched successfully",
                contractMapper.toResponses(contracts)

        );
    }

    @GetMapping("/assigned")
    public ApiResponse<List<ContractResponse>> getAssignedContracts(
            Authentication authentication,
            @RequestParam String assignedTo
    ) {
        User user = authenticateAndFetchUser(userRepo,authentication);
        List<String> roles = fetchRolesForUser(authentication);
        List<ContractDocument> contracts = contractService.getAssignedContracts(
                assignedTo, user, roles
        );

        return ApiResponse.success(
                "Assigned contracts fetched successfully",
                contractMapper.toResponses(contracts)

        );
    }
}