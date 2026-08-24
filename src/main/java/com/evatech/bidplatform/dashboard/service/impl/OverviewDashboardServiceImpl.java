package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.audit.service.AuditAction;
import com.evatech.bidplatform.dashboard.dto.OverviewDashboardResponse;
import com.evatech.bidplatform.dashboard.service.*;
import com.evatech.bidplatform.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @AuditAction(action = "DASHBOARD_OVERVIEW", entity = "Proposal")
    @Override
    public OverviewDashboardResponse getOverview(User user, List<String> roles) {
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
