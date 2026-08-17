package com.evatech.bidplatform.dashboard.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.dashboard.dto.ProposalRequest;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ProposalService {

    Proposal createProposal(ContractDocument contract,
                                    User user,
                                    ProposalRequest request);

    Proposal updateProposal(Proposal proposal, User user,
                            List<String> roles, ProposalRequest request);

    Proposal submitProposal(Proposal proposal, User user);

    Proposal getProposal(Long proposalId, ContractDocument contract);

    Proposal markAsWon(Proposal proposal, User user);

    Proposal markAsLost(Proposal proposal, User user);

    Proposal withdrawProposal(Proposal proposal, User user);

}
