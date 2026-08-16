package com.evatech.bidplatform.dashboard.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.dashboard.dto.ProposalRequest;
import com.evatech.bidplatform.dashboard.dto.ProposalResponse;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import com.evatech.bidplatform.user.entity.User;

public interface ProposalService {

    ProposalResponse createProposal(ContractDocument contract,
                                    User user,
                                    ProposalRequest request);
    ProposalResponse updateProposal(Proposal proposal, ProposalRequest request);

    ProposalResponse submitProposal(Proposal proposal, User user);

    ProposalResponse markAsWon(Proposal proposal, User user);

    ProposalResponse markAsLost(Proposal proposal, User user);

    ProposalResponse withdrawProposal(Proposal proposal, User user);

    Proposal getProposal(Long proposalId, ContractDocument contract);
}
