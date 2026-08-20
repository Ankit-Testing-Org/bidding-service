package com.evatech.bidplatform.dashboard.dto.summary.response;

import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemPriority;
import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemStatus;
import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MyWorkItemResponse(

        Long id,

        String title,

        MyWorkItemType type,

        MyWorkItemStatus status,

        MyWorkItemPriority priority,

        LocalDate dueDate,

        Long relatedObjectId,

        String relatedObjectReference,

        String relatedObjectName,

        BigDecimal value,

        String currency,

        String nextAction,

        LocalDateTime assignedAt,

        LocalDateTime updatedAt

) {
}