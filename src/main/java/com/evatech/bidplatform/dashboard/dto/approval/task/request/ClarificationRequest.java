package com.evatech.bidplatform.dashboard.dto.approval.task.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClarificationRequest(

        @NotBlank(message = "Clarification comment is required")
        @Size(
                max = 4000,
                message = "Clarification comment cannot exceed 4000 characters"
        )
        String comment

) {
}
