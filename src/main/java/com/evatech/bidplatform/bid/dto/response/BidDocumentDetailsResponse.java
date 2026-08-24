package com.evatech.bidplatform.bid.dto.response;

import com.evatech.bidplatform.bid.entity.BidDocumentType;

import java.time.LocalDateTime;

public record BidDocumentDetailsResponse(

        Long documentId,

        Long bidId,

        Long contractId,

        String fileName,

        String originalFileName,

        String contentType,

        Long fileSize,

        BidDocumentType documentType,

        String status,

        String documentUrl,

        String previewUrl,

        LocalDateTime createdAt,

        String createdBy
) {
}