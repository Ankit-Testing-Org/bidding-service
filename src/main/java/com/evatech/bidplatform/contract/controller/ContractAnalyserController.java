package com.evatech.bidplatform.contract.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlight;
import com.evatech.bidplatform.contract.service.ContractHighlightService;
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
    public ApiResponse<List<ContractHighlight>> analyseContract(
            Authentication authentication,
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "false") boolean reanalyse
    ) {
        User user = authenticateAndFetchUser(userRepo,authentication);
        List<String> roles = fetchRolesForUser(authentication);
        List<ContractHighlight> highlights =
                contractHighlightService.analyseContractHightlights(contractId,
                        reanalyse, user, roles);
        return ApiResponse.success("Contract analysis processed successfully", highlights);
    }

}
