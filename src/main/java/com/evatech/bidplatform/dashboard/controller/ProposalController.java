package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.dashboard.dto.ProposalRequest;
import com.evatech.bidplatform.dashboard.dto.ProposalResponse;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import com.evatech.bidplatform.dashboard.mapper.ProposalMapper;
import com.evatech.bidplatform.dashboard.service.ProposalService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proposal")
@RequiredArgsConstructor
public class ProposalController extends AbstractController {

    private final ContractService contractService;
    private final UserRepository userRepository;
    private final ProposalService proposalService;
    private final ProposalMapper proposalMapper;

    @PostMapping("/create")
    public ApiResponse<ProposalResponse> createProposal(Authentication authentication,
                                                        @RequestBody @Valid ProposalRequest request) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contract = contractService.getContract(request.contractDocumentId(), user, roles);

        Proposal proposal = proposalService.createProposal(contract, user, request);

        return ApiResponse.success("Proposal created successfully",
                proposalMapper.toResponse(proposal));
    }

    @PutMapping("/{proposalId}/update")
    public ApiResponse<ProposalResponse> updateProposal(Authentication authentication,
                                                        @PathVariable Long proposalId,
                                                        @RequestBody @Valid ProposalRequest request) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contract = contractService.getContract(request.contractDocumentId(), user, roles);

        Proposal proposal = proposalService.getProposal(proposalId, contract);
        proposal = proposalService.updateProposal(proposal, user , roles, request);
        return ApiResponse.success("Proposal updated successfully",
                proposalMapper.toResponse(proposal));
    }

    @PostMapping("/{proposalId}/submit")
    public ApiResponse<ProposalResponse> submitProposal(Authentication authentication,
                                                        @PathVariable Long proposalId,
                                                        @PathVariable Long contractDocumentId)  {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contract = contractService.getContract(contractDocumentId, user, roles);

        Proposal proposal = proposalService.getProposal(proposalId, contract);

        proposal = proposalService.submitProposal(proposal, user);
        return ApiResponse.success("Proposal submitted successfully",
                proposalMapper.toResponse(proposal));
    }

    @PostMapping("/{proposalId}/won")
    public ApiResponse<ProposalResponse> markAsWon(Authentication authentication,
                                                   @PathVariable Long proposalId,
                                                   @PathVariable Long contractDocumentId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contract = contractService.getContract(contractDocumentId, user, roles);

        Proposal proposal = proposalService.getProposal(proposalId, contract);

        proposal = proposalService.markAsWon(proposal, user);
        return ApiResponse.success("Proposal marked as won",
                proposalMapper.toResponse(proposal));
    }

    @PostMapping("/{proposalId}/lost")
    public ApiResponse<ProposalResponse> markAslost(Authentication authentication,
                                                    @PathVariable Long proposalId,
                                                    @PathVariable Long contractDocumentId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contract = contractService.getContract(contractDocumentId, user, roles);

        Proposal proposal = proposalService.getProposal(proposalId, contract);

        proposal = proposalService.markAsLost(proposal, user);
        return ApiResponse.success("Proposal marked as lost",
                proposalMapper.toResponse(proposal));
    }

    @PostMapping("/{proposalId}/withdraw")
    public ApiResponse<ProposalResponse> withdrawProposal(Authentication authentication,
                                                          @PathVariable Long proposalId,
                                                          @PathVariable Long contractDocumentId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        ContractDocument contract = contractService.getContract(contractDocumentId, user, roles);
        Proposal proposal = proposalService.getProposal(proposalId, contract);

        proposal = proposalService.withdrawProposal(proposal, user);
        return ApiResponse.success("Proposal is withdrawn",
                proposalMapper.toResponse(proposal));
    }
}
