package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.OverviewSummaryDto;
import com.evatech.bidplatform.dashboard.repository.ProposalRepository;
import com.evatech.bidplatform.dashboard.service.DashboardSummaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardSummaryServiceImpl
        implements DashboardSummaryService {

    private final ProposalRepository proposalRepository;

    @Override
    public OverviewSummaryDto getSummary() {

        return OverviewSummaryDto.builder()
                .activeProposalCount(
                        proposalRepository.countActiveProposals())
                .closedProposalCount(
                        proposalRepository.countClosedProposals())
                .wonProposalCount(
                        proposalRepository.countWonProposals())
                .lostProposalCount(
                        proposalRepository.countLostProposals())
                .notBiddedProposalCount(
                        proposalRepository.countNotBiddedProposals())
                .activeProposalValue(
                        proposalRepository.getActiveProposalValue())
                .wonProposalValue(
                        proposalRepository.getWonProposalValue())
                .lostProposalValue(
                        proposalRepository.getLostProposalValue())
                .notBiddedProposalValue(
                        proposalRepository.getNotBiddedProposalValue())
                .build();
    }
}