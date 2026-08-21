package com.evatech.bidplatform.dashboard.dto.approval.task.response;

import java.time.LocalDateTime;

public record ApprovalHistoryResponse(

        Long historyId,

        String action,

        String message,

        String performedBy,

        LocalDateTime performedAt

) {
}
