package com.evatech.bidplatform.dashboard.dto.approval.task.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReassignApprovalTaskRequest(

        @NotNull(message = "Reassigned user ID is required")
        @Positive(message = "Reassigned user ID must be positive")
        Long reassignedUserId,

        @NotBlank(message = "Reassignment reason is required")
        @Size(
                max = 4000,
                message = "Reassignment comment cannot exceed 4000 characters"
        )
        String comment

) {
}