package com.evatech.bidplatform.dashboard.dto.report.response;

import com.evatech.bidplatform.dashboard.dto.report.ReportCategory;
import com.evatech.bidplatform.dashboard.dto.report.ReportPeriod;
import com.evatech.bidplatform.dashboard.dto.report.ReportStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReportDetailResponse(

        Long id,

        String name,

        String description,

        ReportCategory category,

        ReportStatus status,

        ReportPeriod period,

        Long ownerUserId,

        String ownerName,

        LocalDateTime lastRunAt,

        List<String> sourceModules,

        List<ReportMetricResponse> metrics,

        List<ReportInsightResponse> insights,

        String reportViewUrl,

        String pdfExportUrl,

        String excelExportUrl,

        boolean canOpen,

        boolean canExportPdf,

        boolean canExportExcel,

        boolean canSchedule

) {
}