package com.evatech.bidplatform.dashboard.service;

import com.evatech.bidplatform.dashboard.dto.DeadlineDto;

import java.util.List;

public interface DashboardDeadlineService {
    List<DeadlineDto> getDeadlines();
}