package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.report.ReportExportFormat;
import com.evatech.bidplatform.dashboard.dto.report.ReportPeriod;
import com.evatech.bidplatform.dashboard.dto.report.request.ReportScheduleRequest;
import com.evatech.bidplatform.dashboard.dto.report.request.ReportSearchRequest;
import com.evatech.bidplatform.dashboard.dto.report.response.*;
import com.evatech.bidplatform.dashboard.service.ReportDashboardService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDashboardServiceImpl implements ReportDashboardService {

    @Override
    public ReportDashboardResponse fetchDashboard(User user) {
        return null;
    }

    @Override
    public PageResponse<ReportResponse> searchReports(ReportSearchRequest request, User user) {
        return null;
    }

    @Override
    public ReportAnalyticsResponse fetchAnalytics(ReportPeriod period, User user) {
        return null;
    }

    @Override
    public List<ReportActivityResponse> fetchRecentActivities(int limit, User user) {
        return List.of();
    }

    @Override
    public ReportDetailResponse fetchReport(Long reportId, User user) {
        return null;
    }

    @Override
    public ResponseEntity<Resource> viewReport(Long reportId, User user) {
        return null;
    }

    @Override
    public ResponseEntity<Resource> exportReport(Long reportId, ReportExportFormat format, User user) {
        return null;
    }

    @Override
    public ReportScheduleResponse scheduleReport(Long reportId, ReportScheduleRequest request, User user) {
        return null;
    }
}
