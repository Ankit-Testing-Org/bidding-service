package com.evatech.bidplatform.dashboard.service.impl;
import com.evatech.bidplatform.dashboard.dto.PortfolioValuationDto;
import com.evatech.bidplatform.dashboard.entity.ProposalStatus;
import com.evatech.bidplatform.dashboard.repository.ProposalRepository;
import com.evatech.bidplatform.dashboard.service.DashboardValuationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardValuationServiceImpl
        implements DashboardValuationService {

    private final ProposalRepository proposalRepository;

    @Override
    public PortfolioValuationDto getValuation() {

        return PortfolioValuationDto.builder()
                .activeValue(
                        proposalRepository.getActiveProposalValue())
                .wonValue(
                        proposalRepository.getWonProposalValue())
                .lostValue(
                        proposalRepository.getLostProposalValue())
                .notBiddedValue(
                        proposalRepository.getNotBiddedProposalValue())
                .build();
    }

}