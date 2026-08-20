package com.evatech.bidplatform.dashboard.dto.summary.response;

import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemPriority;
import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemType;

public record MyFocusItemResponse(

        Long workItemId,

        MyWorkItemType type,

        MyWorkItemPriority priority,

        String icon,

        String title,

        String description,

        String workspaceUrl

) {
}