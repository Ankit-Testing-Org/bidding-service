package com.evatech.bidplatform.dashboard.dto.report.request;

import com.evatech.bidplatform.dashboard.dto.report.ReportExportFormat;
import com.evatech.bidplatform.dashboard.dto.report.ReportScheduleFrequency;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ReportScheduleRequest(

        @NotNull
        ReportScheduleFrequency frequency,

        @NotNull
        LocalDateTime firstRunAt,

        @NotNull
        String timezone,

        @NotNull
        ReportExportFormat exportFormat,

        @NotEmpty
        List<String> recipientEmails,

        boolean enabled

) {
}