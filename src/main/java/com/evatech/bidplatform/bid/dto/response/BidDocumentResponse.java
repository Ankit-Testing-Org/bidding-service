package com.evatech.bidplatform.bid.dto.response;

import com.evatech.bidplatform.bid.entity.BidDocumentType;

import java.time.LocalDateTime;

public record BidDocumentResponse(
        Long id,
        String fileName,
        BidDocumentType documentType,
        String uploadedBy,
        LocalDateTime uploadedAt
) {
}