package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.OverviewSummaryDto;
import com.evatech.bidplatform.dashboard.repository.ProposalRepository;
import com.evatech.bidplatform.dashboard.service.DashboardSummaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardSummaryServiceImpl
        implements DashboardSummaryService {

    private final ProposalRepository proposalRepository;

    @Override
    public OverviewSummaryDto getSummary() {

        long activeCount = proposalRepository.countActiveProposals();
        long closedCount = proposalRepository.countClosedProposals();
        long wonCount = proposalRepository.countWonProposals();
        long lostCount = proposalRepository.countLostProposals();
        long notBiddedCount = proposalRepository.countNotBiddedProposals();

        BigDecimal activeValue = proposalRepository.getActiveProposalValue();
        BigDecimal wonValue = proposalRepository.getWonProposalValue();
        BigDecimal lostValue = proposalRepository.getLostProposalValue();
        BigDecimal notBiddedValue = proposalRepository.getNotBiddedProposalValue();
        BigDecimal totalValue = proposalRepository.getTotalProposalValue();

        return OverviewSummaryDto.builder()
                .activeProposalCount(activeCount)
                .closedProposalCount(closedCount)
                .wonProposalCount(wonCount)
                .lostProposalCount(lostCount)
                .notBiddedProposalCount(notBiddedCount)
                .totalProposalCount(activeCount + closedCount)
                .activeProposalValue(activeValue)
                .wonProposalValue(wonValue)
                .lostProposalValue(lostValue)
                .notBiddedProposalValue(notBiddedValue)
                .totalProposalValue(totalValue)
                .build();
    }
}