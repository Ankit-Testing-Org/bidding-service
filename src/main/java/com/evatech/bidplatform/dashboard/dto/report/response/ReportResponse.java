package com.evatech.bidplatform.dashboard.dto.report.response;

import com.evatech.bidplatform.dashboard.dto.report.ReportCategory;
import com.evatech.bidplatform.dashboard.dto.report.ReportPeriod;
import com.evatech.bidplatform.dashboard.dto.report.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponse(

        Long id,

        String name,

        String description,

        ReportCategory category,

        ReportStatus status,

        ReportPeriod period,

        Long ownerUserId,

        String ownerName,

        LocalDateTime lastRunAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}