package com.evatech.bidplatform.contract.controller;


import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.dto.request.ApproveAnalysisRequest;
import com.evatech.bidplatform.contract.dto.request.ReanalyseLotRequest;
import com.evatech.bidplatform.contract.dto.response.AnalysisReviewResponse;
import com.evatech.bidplatform.contract.dto.response.ContractLotAnalysisResultResponse;
import com.evatech.bidplatform.contract.dto.response.ContractLotResponse;
import com.evatech.bidplatform.contract.dto.request.RejectAnalysisRequest;
import com.evatech.bidplatform.contract.dto.response.ProposalReadinessResponse;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.mapper.ContractLotMapper;
import com.evatech.bidplatform.contract.service.ContractLotService;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractLotController extends AbstractController {

    private final ContractLotService contractLotService;
    private final ContractLotMapper contractLotMapper;
    private final UserRepository userRepository;
    private final ContractService contractService;

    @GetMapping("/{contractId}/lots")
    public ApiResponse<List<ContractLotResponse>> getContractLots(
            Authentication authentication,
            @PathVariable("contractId")
            @Positive(message = "Contract ID must be positive")
            Long contractId,
            @RequestParam(name = "lotId", required = false) Long lotId
    ) {
        User user = authenticateAndFetchUser(userRepository,authentication);
        List<String> roles = fetchRolesForUser(authentication);
        List<ContractLot> pages = contractService.getContractLots(contractId, user, lotId, roles);
        return ApiResponse.success("Contract pages fetched successfully", contractLotMapper.toResponses(pages));
    }

    /**
     * Loads one lot when the UI needs an authoritative refresh.
     */
    @GetMapping("/{contractId}/lots/{lotId}")
    public ApiResponse<ContractLotResponse> fetchContractLot(
            Authentication authentication,
            @PathVariable("contractId")
            @Positive(message = "Contract ID must be positive")
            Long contractId,
            @PathVariable("lotId")
            @Positive(message = "Lot ID must be positive")
            Long lotId
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        ContractLot response = contractLotService.extractLot(
                        contractId, user, lotId);

        return ApiResponse.success(
                "Contract lot loaded successfully",
                contractLotMapper.toResponse(response)
        );
    }

    @GetMapping("/{contractId}/lots/{lotId}/analyse")
    public ContractLotAnalysisResultResponse analyseContractLot(
            Authentication authentication,

            @PathVariable("contractId")
            @Positive(message = "Contract ID must be positive")
            Long contractId,

            @PathVariable("lotId")
            @Positive(message = "Lot ID must be positive")
            Long lotId,

            @RequestParam(defaultValue = "false") boolean reanalyse) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        return contractLotService.analyseContractLot(lotId,
                reanalyse, user, roles, null);
    }

    @PostMapping("/{contractId}/lots/{lotId}/reanalyse")
    public ResponseEntity<ContractLotAnalysisResultResponse> reanalyseLot(
            Authentication authentication,

            @PathVariable("contractId")
            @Positive(message = "Contract ID must be positive")
            Long contractId,

            @PathVariable("lotId")
            @Positive(message = "Lot ID must be positive")
            Long lotId,
            @Valid @RequestBody ReanalyseLotRequest request) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        ContractLotAnalysisResultResponse result = contractLotService.reanalyseLot(
                lotId, request.getUserComment(), user, roles);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{contractId}/lots/{lotId}/qualification")
    public ApiResponse<ContractLotResponse> qualificationLot(Authentication authentication,
                                                             @PathVariable("contractId")
                                                             @Positive(message = "Contract ID must be positive")
                                                             Long contractId,

                                                             @PathVariable("lotId")
                                                                 @Positive(message = "Lot ID must be positive")
                                                                 Long lotId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        ContractLot contractLot = contractLotService.qualifyLot(contractId, lotId, user,
                roles);

        return ApiResponse.success("Lot qualified successfully", contractLotMapper.toResponse(contractLot));
    }

    @PostMapping("/{contractId}/lots/{lotId}/unqualification")
    public ApiResponse<ContractLotResponse> unqualificationLot(Authentication authentication,
                                                               @PathVariable("contractId")
                                                               @Positive(message = "Contract ID must be positive")
                                                               Long contractId,
                                                               @PathVariable("lotId")
                                                               @Positive(message = "Lot ID must be positive")
                                                                   Long lotId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        ContractLot contractLot = contractLotService.unqualifyLot(contractId, lotId, user,
                roles);

        return ApiResponse.success("Lot unqualified successfully", contractLotMapper.toResponse(contractLot));
    }

    @GetMapping("/{contractId}/lots/qualification")
    public ApiResponse<List<ContractLotResponse>> getQualifiedLots(Authentication authentication,
                                                                   @PathVariable("contractId")
                                                                   @Positive(message = "Contract ID must be positive")
                                                                   Long contractId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<ContractLot> contractLots = contractLotService.getQualifiedLots(contractId, user);
        return ApiResponse.success("Qualified lots fetched successfully", contractLotMapper.toResponses(contractLots));
    }

    @GetMapping("/{contractId}/lots/unqualified")
    public ApiResponse<List<ContractLotResponse>> getUnqualifiedLots(Authentication authentication,
                                                                     @PathVariable("contractId")
                                                                     @Positive(message = "Contract ID must be positive")
                                                                     Long contractId){

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<ContractLot> contractLots = contractLotService.getUnqualifiedLots(contractId, user);
        return ApiResponse.success("UnQualified lots fetched successfully", contractLotMapper.toResponses(contractLots));
    }

    /**
     * Determines whether proposal generation may start.
     */
    @GetMapping("/{contractId}/lots/readiness")
    public ApiResponse<ProposalReadinessResponse> fetchProposalReadiness(
            Authentication authentication,
            @PathVariable("contractId")
            @Positive(message = "Contract ID must be positive")
            Long contractId
    ) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ProposalReadinessResponse response =
                contractLotService.fetchProposalReadiness(contractId, user, roles);

        return ApiResponse.success(
                "Proposal readiness loaded successfully", response);
    }

    @PostMapping("/{contractId}/lots/{lotId}/analysis/approve")
    public ResponseEntity<AnalysisReviewResponse> approveAnalysis(
            Authentication authentication,
            @PathVariable("contractId")
            @Positive(message = "Contract ID must be positive")
            Long contractId,
            @PathVariable("lotId")
            @Positive(message = "Lot ID must be positive")
            Long lotId,
            @RequestBody ApproveAnalysisRequest request) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        ContractLot lot =
                contractLotService.approveAnalysis(lotId, user, request.comment());

        return ResponseEntity.ok(
                toResponse(lot));
    }

    @PostMapping("/{contractId}/lots/{lotId}/analysis/reject")
    public ResponseEntity<AnalysisReviewResponse> rejectAnalysis(
            @PathVariable("contractId")
            @Positive(message = "Contract ID must be positive")
            Long contractId,
            @PathVariable("lotId")
            @Positive(message = "Lot ID must be positive")
            Long lotId,
            @RequestBody RejectAnalysisRequest request,
            @AuthenticationPrincipal User user) {

        ContractLot lot =
                contractLotService.rejectAnalysis(lotId, request.comment(), user);

        return ResponseEntity.ok(
                toResponse(lot));
    }

    private AnalysisReviewResponse toResponse(
            ContractLot lot) {

        return new AnalysisReviewResponse(
                lot.getId(),
                lot.getAnalysisReviewStatus(),
                lot.getReviewComment(),
                lot.getReviewedBy(),
                lot.getReviewedAt());
    }
}

