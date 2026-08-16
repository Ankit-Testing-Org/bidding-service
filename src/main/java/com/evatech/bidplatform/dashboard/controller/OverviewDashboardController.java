package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.dashboard.dto.OverviewDashboardResponse;
import com.evatech.bidplatform.dashboard.service.OverviewDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class OverviewDashboardController extends AbstractController {

    private final OverviewDashboardService dashboardService;

    @GetMapping("/overview")
    public OverviewDashboardResponse getOverview(
            Authentication authentication
            ) {
        return dashboardService.getOverview();
    }
}