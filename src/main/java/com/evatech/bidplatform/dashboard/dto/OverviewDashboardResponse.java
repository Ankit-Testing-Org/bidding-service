package com.evatech.bidplatform.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OverviewDashboardResponse {

    private OverviewSummaryDto summary;

    private ProposalPipelineDto pipeline;

    private PortfolioValuationDto valuation;

    private ReviewWorkloadDto workload;

    private List<DeadlineDto> deadlines;

    private List<ActivityDto> activities;
}