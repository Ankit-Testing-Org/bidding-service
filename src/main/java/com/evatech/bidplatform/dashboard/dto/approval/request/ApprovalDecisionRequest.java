package com.evatech.bidplatform.dashboard.dto.approval.request;

import com.evatech.bidplatform.approval.entity.ApprovalStatus;
import jakarta.validation.constraints.NotNull;

public record ApprovalDecisionRequest(

        @NotNull
        ApprovalStatus decision,

        String comment,

        String delegateUserId

) {
}