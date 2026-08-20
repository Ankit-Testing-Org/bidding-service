package com.evatech.bidplatform.dashboard.dto.report.response;

import java.time.LocalDateTime;

public record ReportViewResponse(

        Long reportId,

        String reportName,

        String viewerUrl,

        LocalDateTime generatedAt

) {
}
