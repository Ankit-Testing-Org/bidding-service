package com.evatech.bidplatform.dashboard.dto.summary.response;

import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemPriority;
import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemType;

import java.time.LocalDate;

public record MyDeadlineResponse(

        Long workItemId,

        MyWorkItemType type,

        MyWorkItemPriority priority,

        LocalDate dueDate,

        String title,

        String description,

        String workspaceUrl

) {
}