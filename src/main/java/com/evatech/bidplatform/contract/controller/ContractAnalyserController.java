package com.evatech.bidplatform.contract.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.service.ContractHighlightService;
import com.evatech.bidplatform.user.dto.response.ContractAnalysisPageResponse;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractAnalyserController extends AbstractController{

    private final UserRepository userRepo;
    private final ContractHighlightService contractHighlightService;

    @GetMapping("/{contractId}/analyse/contract")
    public ApiResponse<ContractAnalysisPageResponse> analyseContract(
            Authentication authentication,
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "false") boolean reanalyse
    ) {
        User user = authenticateAndFetchUser(userRepo,authentication);
        List<String> roles = fetchRolesForUser(authentication);
        ContractAnalysisPageResponse highlights =
                contractHighlightService.analyseContractHighlights(contractId,
                        reanalyse, user, roles);
        return ApiResponse.success("Contract analysis processed successfully", highlights);
    }

}
