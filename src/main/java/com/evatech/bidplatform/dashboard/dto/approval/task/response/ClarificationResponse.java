package com.evatech.bidplatform.dashboard.dto.approval.task.response;

public record ClarificationResponse(

        Long taskId,

        String status,

        String message

) {
}