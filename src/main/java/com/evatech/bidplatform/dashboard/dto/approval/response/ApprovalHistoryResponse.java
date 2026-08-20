package com.evatech.bidplatform.dashboard.dto.approval.response;

import java.time.LocalDateTime;

public record ApprovalHistoryResponse(

        Long id,

        String action,

        String performedBy,

        String comment,

        LocalDateTime createdAt

) {
}