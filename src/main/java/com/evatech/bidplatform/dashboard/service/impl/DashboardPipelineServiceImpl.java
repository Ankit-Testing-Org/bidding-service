package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.ProposalPipelineDto;
import com.evatech.bidplatform.dashboard.entity.ProposalStatus;
import com.evatech.bidplatform.dashboard.repository.ProposalRepository;
import com.evatech.bidplatform.dashboard.service.DashboardPipelineService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardPipelineServiceImpl
        implements DashboardPipelineService {

    private final ProposalRepository proposalRepository;

    @Override
    public ProposalPipelineDto getPipeline() {

        return ProposalPipelineDto.builder()
                .draft(
                        proposalRepository.countByStatus(
                                ProposalStatus.DRAFT))
                .submitted(
                        proposalRepository.countByStatus(
                                ProposalStatus.SUBMITTED))
                .review(
                        proposalRepository.countByStatus(
                                ProposalStatus.IN_REVIEW))
                .awarded(
                        proposalRepository.countByStatus(
                                ProposalStatus.WON))
                .lost(
                        proposalRepository.countByStatus(
                                ProposalStatus.LOST))
                .notBidded(
                        proposalRepository.countByStatus(
                                ProposalStatus.NOT_BIDDED))
                .build();
    }
}