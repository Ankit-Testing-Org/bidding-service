package com.evatech.bidplatform.dashboard.dto.summary.response;

import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemType;

import java.time.LocalDateTime;

public record MyAuditResponse(

        Long id,

        Long relatedObjectId,

        MyWorkItemType type,

        String action,

        String title,

        String description,

        String previousValue,

        String newValue,

        LocalDateTime performedAt,

        String auditTrailUrl

) {
}