package com.evatech.bidplatform.dashboard.service;


import com.evatech.bidplatform.dashboard.dto.OverviewDashboardResponse;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface OverviewDashboardService {
    OverviewDashboardResponse getOverview(User user, List<String> roles);
}