package com.evatech.bidplatform.dashboard.dto.review.response;

import java.time.LocalDateTime;

public record ReviewHistoryResponse(

        Long id,
        String title,
        String text,
        String createdBy,
        LocalDateTime createdAt

) {
}