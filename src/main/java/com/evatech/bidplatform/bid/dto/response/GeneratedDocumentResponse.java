package com.evatech.bidplatform.bid.dto.response;

import java.time.LocalDateTime;

public record GeneratedDocumentResponse(
        Long id,
        String fileName,
        String documentType,
        String status,
        String createdBy,
        LocalDateTime createdAt,
        String downloadUrl
) {
}
