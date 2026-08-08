package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.ActivityDto;
import com.evatech.bidplatform.dashboard.entity.ActivityType;
import com.evatech.bidplatform.dashboard.entity.DashboardActivity;
import com.evatech.bidplatform.dashboard.repository.DashboardActivityRepository;
import com.evatech.bidplatform.dashboard.service.DashboardActivityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardActivityServiceImpl
        implements DashboardActivityService {

    private final DashboardActivityRepository dashboardActivityRepository;

    @Override
    public void logActivity(
            ActivityType activityType,
            String title,
            String description,
            Long referenceId,
            String referenceType,
            String createdBy) {

        DashboardActivity activity = DashboardActivity.builder()
                .activityType(activityType)
                .title(title)
                .description(description)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        dashboardActivityRepository.save(activity);
    }

    @Override
    @Transactional
    public List<ActivityDto> getRecentActivities() {

        return dashboardActivityRepository
                .findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ActivityDto toDto(
            DashboardActivity activity) {

        return ActivityDto.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .activityType(activity.getActivityType())
                .referenceId(activity.getReferenceId())
                .referenceType(activity.getReferenceType())
                .createdBy(activity.getCreatedBy())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
