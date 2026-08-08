package com.evatech.bidplatform.dashboard.service;

import com.evatech.bidplatform.dashboard.dto.ActivityDto;
import com.evatech.bidplatform.dashboard.entity.ActivityType;

import java.util.List;

public interface DashboardActivityService {

    void logActivity(
            ActivityType activityType,
            String title,
            String description,
            Long referenceId,
            String referenceType,
            String user);

    List<ActivityDto> getRecentActivities();
}