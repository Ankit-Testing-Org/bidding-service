package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.service.ContractLotService;
import com.evatech.bidplatform.dashboard.dto.ProposalRequest;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import com.evatech.bidplatform.dashboard.entity.ProposalHistory;
import com.evatech.bidplatform.dashboard.entity.ProposalStatus;
import com.evatech.bidplatform.dashboard.mapper.ProposalMapper;
import com.evatech.bidplatform.dashboard.repository.ProposalHistoryRepository;
import com.evatech.bidplatform.dashboard.repository.ProposalRepository;
import com.evatech.bidplatform.dashboard.service.ProposalService;
import com.evatech.bidplatform.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProposalServiceImpl implements ProposalService {

    private final ProposalMapper proposalMapper;
    private final ProposalRepository proposalRepository;
    private final ProposalHistoryRepository proposalHistoryRepository;
    private final ContractLotService contractLotService;

    @Override
    @Transactional
    public Proposal createProposal(ContractDocument contract, User user,
                                           ProposalRequest request) {
        Proposal proposal = new Proposal();
        proposal.setProposalNumber(generateProposalNumber());
        proposal.setTitle(request.title());
        proposal.setContractDocument(contract);
        proposal.setSubmissionDate(request.submissionDate());
        proposal.setStatus(ProposalStatus.DRAFT);
        proposal.setCreatedBy(user.getEmail());
        proposal.setCreatedAt(LocalDateTime.now());
        proposal.setUpdatedAt(LocalDateTime.now());
        proposal.setProposalValue(BigDecimal.ZERO);
        proposal = proposalRepository.save(proposal);
        ProposalHistory proposalHistory = createHistory(proposal, null, ProposalStatus.DRAFT, user.getEmail());
        proposal.getHistory().add(proposalHistory);
        return proposal;
    }

    @Override
    @Transactional
    public Proposal updateProposal(Proposal proposal, User user,
                                   List<String> roles,
                                   ProposalRequest request) {
        ProposalStatus proposalStatus = proposal.getStatus();
        if(request != null) {
            proposal.setTitle(request.title());
            proposal.setSubmissionDate(request.submissionDate());
            proposalStatus = request.proposalStatus();
        }
        if(!proposal.getStatus().name().equals(proposalStatus.name())) {
            ProposalHistory proposalHistory =
                    createHistory(proposal, proposal.getStatus(), proposalStatus, user.getEmail());
            proposal.getHistory().add(proposalHistory);

        }
        List<ContractLot> contractLots = contractLotService.getContractLots(proposal.getContractDocument().getId(), user, roles);
        proposal.setProposalValue(calculateProposalValue(contractLots));
        proposal.setUpdatedAt(LocalDateTime.now());
        proposal = proposalRepository.save(proposal);

        return proposal;
    }

    @Override
    @Transactional
    public Proposal submitProposal(Proposal proposal, User user) {

        proposal.setStatus(ProposalStatus.SUBMITTED);
        proposal.setSubmissionDate(LocalDate.now());
        proposal.setUpdatedAt(LocalDateTime.now());
        ProposalStatus oldStatus = proposal.getStatus();
        proposal.setStatus(ProposalStatus.SUBMITTED);
        proposal.setSubmissionDate(LocalDate.now());
        proposal.setUpdatedAt(LocalDateTime.now());
        proposal = proposalRepository.save(proposal);

        createHistory(proposal, oldStatus, ProposalStatus.SUBMITTED, user.getEmail());

        return proposal;
    }

    @Override
    @Transactional
    public Proposal markAsWon(Proposal proposal, User user) {

        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted proposals can be marked as won");
        }

        ProposalStatus oldStatus = proposal.getStatus();
        proposal.setStatus(ProposalStatus.WON);
        proposal.setUpdatedAt(LocalDateTime.now());

        createHistory(proposal, oldStatus, ProposalStatus.WON, user.getEmail());

        return proposal;
    }

    @Override
    @Transactional
    public Proposal markAsLost(Proposal proposal, User user) {

        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted proposals can be marked as lost");
        }

        ProposalStatus oldStatus = proposal.getStatus();
        proposal.setStatus(ProposalStatus.LOST);
        proposal.setUpdatedAt(LocalDateTime.now());

        createHistory(proposal, oldStatus, ProposalStatus.LOST, user.getEmail());

        return proposal;
    }

    @Override
    @Transactional
    public Proposal withdrawProposal(Proposal proposal, User user) {
        if (proposal.getStatus() == ProposalStatus.WON) {
            throw new IllegalStateException("Won proposal cannot be withdrawn");
        }
        ProposalStatus oldStatus = proposal.getStatus();
        proposal.setStatus(ProposalStatus.WITHDRAWN);
        proposal.setUpdatedAt(LocalDateTime.now());

        createHistory(proposal, oldStatus, ProposalStatus.WITHDRAWN, user.getEmail());

        return proposal;
    }

    @Override
    public Proposal getProposal(Long proposalId, ContractDocument contract) {
        if(proposalId != null && contract != null)
            return proposalRepository.getByIdAndContractDocument(proposalId, contract);
        else if(proposalId == null && contract != null)
            return proposalRepository.getByContractDocument(contract);

        throw new RuntimeException("Proposal Id or Contract document either of field is mandatory");
    }


    private String generateProposalNumber() {

        long count = proposalRepository.count() + 1;
        return String.format("PROP-%d-%06d", LocalDate.now().getYear(), count);
    }

    private BigDecimal calculateProposalValue(List<ContractLot> contractLots) {
        return contractLots.stream().
                filter(ContractLot::isQualified).
                map(ContractLot::getValuation).
                filter(Objects::nonNull).
                reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ProposalHistory createHistory(Proposal proposal, ProposalStatus oldStatus, ProposalStatus newStatus, String user) {

        ProposalHistory history = new ProposalHistory();
        history.setProposal(proposal);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setActionBy(user);
        history.setActionAt(LocalDateTime.now());

        return proposalHistoryRepository.save(history);
    }
}
