package com.evatech.bidplatform.dashboard.dto;

import com.evatech.bidplatform.dashboard.entity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDto {

    private Long id;

    private String title;

    private String description;

    private ActivityType activityType;

    private Long referenceId;

    private String referenceType;

    private String createdBy;

    private LocalDateTime createdAt;
}