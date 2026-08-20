package com.evatech.bidplatform.dashboard.dto.summary.request;

import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemStatus;
import com.evatech.bidplatform.dashboard.dto.summary.MyWorkItemType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record MyWorkItemSearchRequest(

        String searchText,

        MyWorkItemType type,

        MyWorkItemStatus status,

        @Min(0)
        Integer page,

        @Min(1)
        @Max(100)
        Integer size,

        String sortBy,

        @Pattern(regexp = "(?i)ASC|DESC")
        String sortDirection

) {
}