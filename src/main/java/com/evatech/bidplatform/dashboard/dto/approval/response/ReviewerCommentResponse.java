package com.evatech.bidplatform.dashboard.dto.approval.response;

import java.time.LocalDateTime;

public record ReviewerCommentResponse(

        Long id,

        String reviewerName,

        String comment,

        LocalDateTime createdAt

) {
}