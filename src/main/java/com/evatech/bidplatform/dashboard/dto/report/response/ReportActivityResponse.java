package com.evatech.bidplatform.dashboard.dto.report.response;

import java.time.LocalDateTime;

public record ReportActivityResponse(

        Long id,

        Long reportId,

        String reportName,

        String action,

        String description,

        String performedBy,

        LocalDateTime performedAt

) {
}