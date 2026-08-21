package com.evatech.bidplatform.dashboard.dto.approval.task.response;

public record ReassignApprovalTaskResponse(

        Long taskId,

        Long reassignedUserId,

        String reassignedUserName,

        String status,

        String message

) {
}