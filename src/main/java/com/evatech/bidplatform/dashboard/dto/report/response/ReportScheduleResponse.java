package com.evatech.bidplatform.dashboard.dto.report.response;

import com.evatech.bidplatform.dashboard.dto.report.ReportExportFormat;
import com.evatech.bidplatform.dashboard.dto.report.ReportScheduleFrequency;

import java.time.LocalDateTime;
import java.util.List;

public record ReportScheduleResponse(

        Long scheduleId,

        Long reportId,

        ReportScheduleFrequency frequency,

        LocalDateTime nextRunAt,

        String timezone,

        ReportExportFormat exportFormat,

        List<String> recipientEmails,

        boolean enabled

) {
}