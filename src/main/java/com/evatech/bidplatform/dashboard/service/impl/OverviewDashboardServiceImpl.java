package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.OverviewDashboardResponse;
import com.evatech.bidplatform.dashboard.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class OverviewDashboardServiceImpl
        implements OverviewDashboardService {

    private final DashboardSummaryService dashboardSummaryService;
    private final DashboardPipelineService dashboardPipelineService;
    private final DashboardValuationService dashboardValuationService;
    private final DashboardDeadlineService dashboardDeadlineService;
    private final DashboardWorkloadService dashboardWorkloadService;
    private final DashboardActivityService dashboardActivityService;

    @Override
    public OverviewDashboardResponse getOverview() {

        return OverviewDashboardResponse.builder()
                .summary(dashboardSummaryService.getSummary())
                .pipeline(dashboardPipelineService.getPipeline())
                .valuation(dashboardValuationService.getValuation())
                .deadlines(dashboardDeadlineService.getDeadlines())
                .workload(dashboardWorkloadService.getWorkload())
                .activities(dashboardActivityService.getRecentActivities())
                .build();
    }
}
