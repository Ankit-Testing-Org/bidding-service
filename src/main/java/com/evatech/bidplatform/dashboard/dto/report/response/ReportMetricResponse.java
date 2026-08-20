package com.evatech.bidplatform.dashboard.dto.report.response;

public record ReportMetricResponse(

        String code,

        String title,

        String description,

        String formattedValue

) {
}