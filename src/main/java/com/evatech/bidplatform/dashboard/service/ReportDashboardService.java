package com.evatech.bidplatform.dashboard.service;

import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.report.ReportExportFormat;
import com.evatech.bidplatform.dashboard.dto.report.ReportPeriod;
import com.evatech.bidplatform.dashboard.dto.report.request.ReportScheduleRequest;
import com.evatech.bidplatform.dashboard.dto.report.request.ReportSearchRequest;
import com.evatech.bidplatform.dashboard.dto.report.response.*;
import com.evatech.bidplatform.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReportDashboardService {
    ReportDashboardResponse fetchDashboard(User user);

    PageResponse<ReportResponse> searchReports(@Valid ReportSearchRequest request, User user);

    ReportAnalyticsResponse fetchAnalytics(ReportPeriod period, User user);

    List<ReportActivityResponse> fetchRecentActivities(int limit, User user);

    ReportDetailResponse fetchReport(Long reportId, User user);

    ResponseEntity<Resource> viewReport(Long reportId, User user);

    ResponseEntity<Resource> exportReport(Long reportId, ReportExportFormat format, User user);

    ReportScheduleResponse scheduleReport(Long reportId, @Valid ReportScheduleRequest request, User user);
}
