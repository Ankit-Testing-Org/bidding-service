package com.evatech.bidplatform.dashboard.dto.approval.task.response;

import java.time.LocalDateTime;

public record ApprovalDecisionResponse(

        Long taskId,

        String taskStatus,

        String decision,

        LocalDateTime decidedAt,

       String decidedBy) {

}