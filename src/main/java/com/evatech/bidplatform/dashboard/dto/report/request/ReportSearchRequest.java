package com.evatech.bidplatform.dashboard.dto.report.request;

import com.evatech.bidplatform.dashboard.dto.report.ReportCategory;
import com.evatech.bidplatform.dashboard.dto.report.ReportPeriod;
import com.evatech.bidplatform.dashboard.dto.report.ReportStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record ReportSearchRequest(

        String searchText,

        ReportCategory category,

        ReportStatus status,

        ReportPeriod period,

        @Min(0)
        Integer page,

        @Min(1)
        @Max(100)
        Integer size,

        String sortBy,

        @Pattern(regexp = "(?i)ASC|DESC")
        String sortDirection

) {
}