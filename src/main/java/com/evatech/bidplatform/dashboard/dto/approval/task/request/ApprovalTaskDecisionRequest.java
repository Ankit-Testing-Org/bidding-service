package com.evatech.bidplatform.dashboard.dto.approval.task.request;


import com.evatech.bidplatform.approval.entity.ApprovalDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApprovalTaskDecisionRequest(

        @NotNull(message = "Approval decision is required")
        ApprovalDecision decision,

        @Size(
                max = 4000,
                message = "Decision comment cannot exceed 4000 characters"
        )
        String comment

) {
}