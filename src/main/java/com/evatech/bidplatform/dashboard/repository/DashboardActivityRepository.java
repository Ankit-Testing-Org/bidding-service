package com.evatech.bidplatform.dashboard.repository;

import com.evatech.bidplatform.dashboard.entity.ActivityType;
import com.evatech.bidplatform.dashboard.entity.DashboardActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardActivityRepository
        extends JpaRepository<DashboardActivity, Long> {

    List<DashboardActivity> findTop20ByOrderByCreatedAtDesc();

    List<DashboardActivity> findTop10ByOrderByCreatedAtDesc();

    List<DashboardActivity> findByActivityTypeOrderByCreatedAtDesc(
            ActivityType activityType);

    List<DashboardActivity> findByCreatedByOrderByCreatedAtDesc(
            String createdBy);

    List<DashboardActivity> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from,
            LocalDateTime to);
}