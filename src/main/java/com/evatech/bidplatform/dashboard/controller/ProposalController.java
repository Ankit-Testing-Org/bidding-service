package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.dashboard.dto.OverviewDashboardResponse;
import com.evatech.bidplatform.dashboard.dto.ProposalRequest;
import com.evatech.bidplatform.dashboard.dto.ProposalResponse;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import com.evatech.bidplatform.dashboard.service.OverviewDashboardService;
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

    @PostMapping
    public ProposalResponse createProposal(Authentication authentication, @RequestBody @Valid ProposalRequest request) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contract = contractService.getContract(request.contractDocumentId(), user, roles);

        return proposalService.createProposal(contract, user, request);
    }

    @PutMapping("/{proposalId}")
    public ProposalResponse updateProposal(Authentication authentication, @PathVariable Long proposalId, @RequestBody @Valid ProposalRequest request) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contract = contractService.getContract(request.contractDocumentId(), user, roles);

        Proposal proposal = proposalService.getProposal(proposalId, contract);
        return proposalService.updateProposal(proposal, request);
    }

    @PostMapping("/{proposalId}/submit")
    public ProposalResponse submitProposal(Authentication authentication, @PathVariable Long proposalId, @PathVariable Long contractDocumentId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contract = contractService.getContract(contractDocumentId, user, roles);

        Proposal proposal = proposalService.getProposal(proposalId, contract);

        return proposalService.submitProposal(proposal, user);
    }

    @PostMapping("/{proposalId}/won")
    public ProposalResponse markAsWon(Authentication authentication, @PathVariable Long proposalId, @PathVariable Long contractDocumentId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contract = contractService.getContract(contractDocumentId, user, roles);

        Proposal proposal = proposalService.getProposal(proposalId, contract);

        return proposalService.markAsWon(proposal, user);
    }

    @PostMapping("/{proposalId}/lost")
    public ProposalResponse markAslost(Authentication authentication, @PathVariable Long proposalId, @PathVariable Long contractDocumentId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        ContractDocument contract = contractService.getContract(contractDocumentId, user, roles);

        Proposal proposal = proposalService.getProposal(proposalId, contract);

        return proposalService.markAsLost(proposal, user);
    }

    @PostMapping("/{proposalId}/withdraw")
    public ProposalResponse withdrawProposal(Authentication authentication, @PathVariable Long proposalId, @PathVariable Long contractDocumentId) {

        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        ContractDocument contract = contractService.getContract(contractDocumentId, user, roles);
        Proposal proposal = proposalService.getProposal(proposalId, contract);

        return proposalService.withdrawProposal(proposal, user);
    }
}
