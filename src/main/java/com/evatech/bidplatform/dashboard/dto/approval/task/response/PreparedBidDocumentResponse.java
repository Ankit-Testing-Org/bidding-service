package com.evatech.bidplatform.dashboard.dto.approval.task.response;

import java.time.LocalDateTime;

public record PreparedBidDocumentResponse(

        Long documentId,

        String fileName,

        String fileType,

        String previewUrl,

        String downloadUrl,

        Long fileSizeInBytes,

        LocalDateTime uploadedAt,

        String uploadedBy

) {
}