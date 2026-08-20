package com.evatech.bidplatform.dashboard.dto.review.response;

import com.evatech.bidplatform.dashboard.dto.review.ReviewPriority;
import com.evatech.bidplatform.dashboard.dto.review.ReviewStatus;
import com.evatech.bidplatform.dashboard.dto.review.ReviewType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReviewResponse(

        Long id,

        String title,

        ReviewType reviewType,

        ReviewStatus status,

        ReviewPriority priority,

        Long sourceId,

        String sourceName,

        String reviewerUserId,

        String reviewerName,

        LocalDate dueDate,

        LocalDateTime createdAt

) {
}