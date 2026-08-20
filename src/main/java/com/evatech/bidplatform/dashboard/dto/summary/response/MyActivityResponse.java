package com.evatech.bidplatform.dashboard.dto.summary.response;

import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemType;

import java.time.LocalDateTime;

public record MyActivityResponse(

        Long id,

        Long relatedObjectId,

        MyWorkItemType type,

        String action,

        String title,

        String description,

        LocalDateTime performedAt,

        String workspaceUrl

) {
}