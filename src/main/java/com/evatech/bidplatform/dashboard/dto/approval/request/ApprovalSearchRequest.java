package com.evatech.bidplatform.dashboard.dto.approval.request;

import com.evatech.bidplatform.approval.entity.ApprovalStage;
import com.evatech.bidplatform.approval.entity.ApprovalStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ApprovalSearchRequest(

        String searchText,

        ApprovalStatus status,

        ApprovalStage approvalStage,

        @Min(0)
        Integer page,

        @Min(1)
        @Max(100)
        Integer size,

        String sortBy,

        String sortDirection

) {
}