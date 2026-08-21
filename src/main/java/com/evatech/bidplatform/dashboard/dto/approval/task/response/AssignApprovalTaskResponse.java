package com.evatech.bidplatform.dashboard.dto.approval.task.response;

public record AssignApprovalTaskResponse(

        Long taskId,

        String assignedTo,

        String status,

        boolean canApprove,

        String message) {
}

