package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.dashboard.dto.ProposalRequest;
import com.evatech.bidplatform.dashboard.dto.ProposalResponse;
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
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProposalServiceImpl implements ProposalService {

    private final ProposalMapper proposalMapper;
    private final ProposalRepository proposalRepository;
    private final ProposalHistoryRepository proposalHistoryRepository;

    @Override
    @Transactional
    public ProposalResponse createProposal(ContractDocument contract, User user, ProposalRequest request) {
        Proposal proposal = new Proposal();
        proposal.setProposalNumber(generateProposalNumber());
        proposal.setTitle(request.title());
        proposal.setContractDocument(contract);
        proposal.setSubmissionDate(request.submissionDate());
        proposal.setStatus(ProposalStatus.DRAFT);
        proposal.setCreatedBy(user.getEmail());
        proposal.setCreatedAt(LocalDateTime.now());
        proposal.setUpdatedAt(LocalDateTime.now());
        proposal.setProposalValue(calculateProposalValue(contract));
        proposal = proposalRepository.save(proposal);
        createHistory(proposal, null, ProposalStatus.DRAFT, user.getEmail());
        return proposalMapper.toResponse(proposal);
    }

    @Override
    @Transactional
    public ProposalResponse updateProposal(Proposal proposal, ProposalRequest request) {
        proposal.setTitle(request.title());
        proposal.setSubmissionDate(request.submissionDate());
        proposal.setUpdatedAt(LocalDateTime.now());
        proposal.setProposalValue(calculateProposalValue(proposal.getContractDocument()));
        proposal = proposalRepository.save(proposal);
        return proposalMapper.toResponse(proposal);
    }

    @Override
    @Transactional
    public ProposalResponse submitProposal(Proposal proposal, User user) {

        proposal.setStatus(ProposalStatus.SUBMITTED);
        proposal.setSubmissionDate(LocalDate.now());
        proposal.setUpdatedAt(LocalDateTime.now());
        ProposalStatus oldStatus = proposal.getStatus();
        proposal.setStatus(ProposalStatus.SUBMITTED);
        proposal.setSubmissionDate(LocalDate.now());
        proposal.setUpdatedAt(LocalDateTime.now());
        proposal = proposalRepository.save(proposal);

        createHistory(proposal, oldStatus, ProposalStatus.SUBMITTED, user.getEmail());

        return proposalMapper.toResponse(proposal);
    }

    @Override
    @Transactional
    public ProposalResponse markAsWon(Proposal proposal, User user) {

        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted proposals can be marked as won");
        }

        ProposalStatus oldStatus = proposal.getStatus();
        proposal.setStatus(ProposalStatus.WON);
        proposal.setUpdatedAt(LocalDateTime.now());

        createHistory(proposal, oldStatus, ProposalStatus.WON, user.getEmail());

        return proposalMapper.toResponse(proposal);
    }

    @Override
    @Transactional
    public ProposalResponse markAsLost(Proposal proposal, User user) {

        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted proposals can be marked as lost");
        }

        ProposalStatus oldStatus = proposal.getStatus();
        proposal.setStatus(ProposalStatus.LOST);
        proposal.setUpdatedAt(LocalDateTime.now());

        createHistory(proposal, oldStatus, ProposalStatus.LOST, user.getEmail());

        return proposalMapper.toResponse(proposal);
    }

    @Override
    @Transactional
    public ProposalResponse withdrawProposal(Proposal proposal, User user) {
        if (proposal.getStatus() == ProposalStatus.WON) {
            throw new IllegalStateException("Won proposal cannot be withdrawn");
        }
        ProposalStatus oldStatus = proposal.getStatus();
        proposal.setStatus(ProposalStatus.WITHDRAWN);
        proposal.setUpdatedAt(LocalDateTime.now());

        createHistory(proposal, oldStatus, ProposalStatus.WITHDRAWN, user.getEmail());

        return proposalMapper.toResponse(proposal);
    }

    @Override
    public Proposal getProposal(Long proposalId, ContractDocument contract) {
        return proposalRepository.getByIdAndContractDocument(proposalId, contract);
    }


    private String generateProposalNumber() {

        long count = proposalRepository.count() + 1;
        return String.format("PROP-%d-%06d", LocalDate.now().getYear(), count);
    }

    private BigDecimal calculateProposalValue(ContractDocument contractDocument) {
        return contractDocument.getLots().stream().map(ContractLot::getValuation).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void createHistory(Proposal proposal, ProposalStatus oldStatus, ProposalStatus newStatus, String user) {

        ProposalHistory history = new ProposalHistory();
        history.setProposal(proposal);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setActionBy(user);
        history.setActionAt(LocalDateTime.now());

        proposalHistoryRepository.save(history);
    }
}
