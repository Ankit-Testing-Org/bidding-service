package com.evatech.bidplatform.contract.controller;


import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.dto.ContractLotAnalysisResult;
import com.evatech.bidplatform.contract.dto.ReanalyseLotRequest;
import com.evatech.bidplatform.contract.dto.response.ContractLotResponse;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.mapper.ContractLotMapper;
import com.evatech.bidplatform.contract.service.ContractLotService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
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

    @PostMapping("/{contractId}/lots/{lotNumber}/qualify")
    public ApiResponse<ContractLotResponse> qualifyLot(Authentication authentication, @PathVariable Long contractId, @PathVariable String lotNumber) {

        User user = authenticateAndFetchUser(userRepository, authentication);

        ContractLot contractLot = contractLotService.qualifyLot(contractId, lotNumber, user);

        return ApiResponse.success("Lot qualified successfully", contractLotMapper.toResponse(contractLot));
    }

    @PostMapping("/{contractId}/lots/{lotNumber}/unqualify")
    public ApiResponse<ContractLotResponse> unqualifyLot(Authentication authentication, @PathVariable Long contractId, @PathVariable String lotNumber) {

        User user = authenticateAndFetchUser(userRepository, authentication);

        ContractLot contractLot = contractLotService.unqualifyLot(contractId, lotNumber, user);

        return ApiResponse.success("Lot unqualified successfully", contractLotMapper.toResponse(contractLot));
    }

    @GetMapping("/{contractId}/lots")
    public ApiResponse<List<ContractLotResponse>> getContractLots(Authentication authentication, @PathVariable Long contractId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        List<ContractLot> contractLots = contractLotService.getContractLots(contractId, user, roles);

        return ApiResponse.success("Contract lots fetched successfully", contractLotMapper.toResponses(contractLots));
    }

    @GetMapping("/{contractId}/lots/qualified")
    public ApiResponse<List<ContractLotResponse>> getQualifiedLots(Authentication authentication, @PathVariable Long contractId) {

        User user = authenticateAndFetchUser(userRepository, authentication);

        List<ContractLot> contractLots = contractLotService.getQualifiedLots(contractId, user);

        return ApiResponse.success("Qualified lots fetched successfully", contractLotMapper.toResponses(contractLots));
    }

    @GetMapping("/{contractId}/lots/unqualified")
    public ApiResponse<List<ContractLotResponse>> getUnqualifiedLots(Authentication authentication, @PathVariable Long contractId) {

        User user = authenticateAndFetchUser(userRepository, authentication);

        List<ContractLot> contractLots = contractLotService.getUnqualifiedLots(contractId, user);

        return ApiResponse.success("UnQualified lots fetched successfully", contractLotMapper.toResponses(contractLots));
    }

    @PostMapping("/api/contracts/lots/{contractLotId}/analyse")
    public ContractLotAnalysisResult analyseContractLot(
            Authentication authentication,
            @PathVariable Long contractLotId,
            @RequestParam(defaultValue = "false") boolean reanalyse) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        return contractLotService.analyseContractLot(contractLotId,
                reanalyse, user, roles, null);
    }

    @PostMapping("/{lotId}/reanalyse")
    public ResponseEntity<ContractLotAnalysisResult> reanalyseLot(
            Authentication authentication,
            @PathVariable Long lotId,
            @Valid @RequestBody ReanalyseLotRequest request) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        ContractLotAnalysisResult result = contractLotService.reanalyseLot(
                lotId, request.getUserComment(), user, roles);
        return ResponseEntity.ok(result);
    }
}

