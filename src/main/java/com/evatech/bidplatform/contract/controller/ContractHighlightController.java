package com.evatech.bidplatform.contract.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.dto.request.HighlightReviewRequest;
import com.evatech.bidplatform.contract.service.ContractHighlightService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.dto.response.ContractHighlightResponse;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractHighlightController extends AbstractController {

    private final UserRepository userRepo;
    private final ContractService contractService;
    private final ContractHighlightService contractHighlightService;

    @GetMapping("/{contractId}/fetch/contract/highlights")
    public ApiResponse<List<ContractHighlightResponse>> getHighlights(Authentication authentication,
                                                                      @PathVariable Long contractId) {
        User user = authenticateAndFetchUser(userRepo, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        List<ContractHighlightResponse> highlights = contractService.getHighlights(contractId, user, roles);

        return ApiResponse.success("Contract highlights fetched successfully", highlights);
    }

    @PostMapping("/contract/highlights/{highlightId}/approve")
    public ApiResponse<ContractHighlightResponse> approveHighlight(Authentication authentication, @PathVariable Long highlightId) {
        User user = authenticateAndFetchUser(userRepo, authentication);
        ContractHighlightResponse highlight = contractHighlightService.approveHighlight(highlightId, user);

        return ApiResponse.success("Highlight approved successfully", highlight);
    }

    @PostMapping("/contract/highlights/{highlightId}/reject")
    public ApiResponse<ContractHighlightResponse> rejectHighlight(Authentication authentication,
                                                                  @PathVariable Long highlightId,
                                                                  @RequestBody HighlightReviewRequest request) {
        User user = authenticateAndFetchUser(userRepo, authentication);
        ContractHighlightResponse highlight = contractHighlightService.rejectHighlight(highlightId, request.comment(), user);

        return ApiResponse.success("Highlight rejected successfully", highlight);
    }

    @PostMapping("/contract/highlights/{highlightId}/reanalyse")
    public ApiResponse<ContractHighlightResponse> reanalyseHighlight(Authentication authentication,
                                                                     @PathVariable Long highlightId,
                                                                     @RequestBody HighlightReviewRequest request) {

        User user = authenticateAndFetchUser(userRepo, authentication);
        ContractHighlightResponse highlight = contractHighlightService.reanalyseHighlight(highlightId, user,  request.comment());

        return ApiResponse.success("Highlight reanalysed successfully", highlight);
    }
}
