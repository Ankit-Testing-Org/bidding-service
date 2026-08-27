package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.common.controller.AbstractController;
import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.report.ReportExportFormat;
import com.evatech.bidplatform.dashboard.dto.report.ReportPeriod;
import com.evatech.bidplatform.dashboard.dto.report.request.ReportScheduleRequest;
import com.evatech.bidplatform.dashboard.dto.report.request.ReportSearchRequest;
import com.evatech.bidplatform.dashboard.dto.report.response.ReportActivityResponse;
import com.evatech.bidplatform.dashboard.dto.report.response.ReportAnalyticsResponse;
import com.evatech.bidplatform.dashboard.dto.report.response.ReportDashboardResponse;
import com.evatech.bidplatform.dashboard.dto.report.response.ReportDetailResponse;
import com.evatech.bidplatform.dashboard.dto.report.response.ReportResponse;
import com.evatech.bidplatform.dashboard.dto.report.response.ReportScheduleResponse;
import com.evatech.bidplatform.dashboard.service.ReportDashboardService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportDashboardController extends AbstractController {

    private final UserRepository userRepository;
    private final ReportDashboardService reportDashboardService;


    @GetMapping("/dashboard")
    public ApiResponse<ReportDashboardResponse> fetchDashboard() {
        User user = authenticateAndFetchUser(
                userRepository);

        ReportDashboardResponse response =
                reportDashboardService.fetchDashboard(user);

        return ApiResponse.success(
                "Report dashboard fetched successfully",
                response
        );
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse<ReportResponse>> searchReports(
            @RequestBody @Valid ReportSearchRequest request
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        PageResponse<ReportResponse> response =
                reportDashboardService.searchReports(request, user);

        return ApiResponse.success(
                "Reports returned successfully",
                response
        );
    }

    @GetMapping("/analytics")
    public ApiResponse<ReportAnalyticsResponse> fetchAnalytics(
            @RequestParam(
                    name = "period",
                    required = false
            ) ReportPeriod period
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        ReportAnalyticsResponse response =
                reportDashboardService.fetchAnalytics(period, user);

        return ApiResponse.success(
                "Report analytics fetched successfully",
                response
        );
    }

    @GetMapping("/activities")
    public ApiResponse<List<ReportActivityResponse>> fetchActivities(
            @RequestParam(
                    name = "limit",
                    defaultValue = "10"
            ) int limit
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        List<ReportActivityResponse> response =
                reportDashboardService.fetchRecentActivities(
                        limit,
                        user
                );

        return ApiResponse.success(
                "Recent report activities fetched successfully",
                response
        );
    }

    @GetMapping("/{reportId}")
    public ApiResponse<ReportDetailResponse> fetchReport(
            @PathVariable("reportId") Long reportId
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        ReportDetailResponse response =
                reportDashboardService.fetchReport(
                        reportId,
                        user
                );

        return ApiResponse.success(
                "Report details fetched successfully",
                response
        );
    }

    @GetMapping("/{reportId}/view")
    public ResponseEntity<Resource> viewReport(
            @PathVariable("reportId") Long reportId
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        return reportDashboardService.viewReport(
                reportId,
                user
        );
    }

    @GetMapping("/{reportId}/export")
    public ResponseEntity<Resource> exportReport(
            @PathVariable("reportId") Long reportId,
            @RequestParam("format") ReportExportFormat format
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        return reportDashboardService.exportReport(
                reportId,
                format,
                user
        );
    }

    @PostMapping("/{reportId}/schedule")
    public ApiResponse<ReportScheduleResponse> scheduleReport(
            @PathVariable("reportId") Long reportId,
            @RequestBody @Valid ReportScheduleRequest request
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        ReportScheduleResponse response =
                reportDashboardService.scheduleReport(
                        reportId,
                        request,
                        user
                );

        return ApiResponse.success(
                "Report scheduled successfully",
                response
        );
    }
}