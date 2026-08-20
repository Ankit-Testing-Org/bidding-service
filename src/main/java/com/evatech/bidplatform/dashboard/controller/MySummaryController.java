package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.summary.request.MyWorkItemSearchRequest;
import com.evatech.bidplatform.dashboard.dto.summary.response.*;
import com.evatech.bidplatform.dashboard.service.MySummaryService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-summary")
@RequiredArgsConstructor
public class MySummaryController extends AbstractController {

    private final UserRepository userRepository;
    private final MySummaryService mySummaryService;

    @GetMapping("/dashboard")
    public ApiResponse<MySummaryDashboardResponse> fetchDashboard(
            Authentication authentication
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        MySummaryDashboardResponse response =
                mySummaryService.fetchDashboard(user);

        return ApiResponse.success(
                "My summary dashboard fetched successfully",
                response
        );
    }

    @PostMapping("/work-items/search")
    public ApiResponse<PageResponse<MyWorkItemResponse>> searchWorkItems(
            Authentication authentication,
            @RequestBody @Valid MyWorkItemSearchRequest request
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        PageResponse<MyWorkItemResponse> response =
                mySummaryService.searchWorkItems(
                        request,
                        user
                );

        return ApiResponse.success(
                "My work items fetched successfully",
                response
        );
    }

    @GetMapping("/work-items/{workItemId}")
    public ApiResponse<MyWorkItemDetailResponse> fetchWorkItem(
            Authentication authentication,
            @PathVariable("workItemId") Long workItemId
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        MyWorkItemDetailResponse response =
                mySummaryService.fetchWorkItem(
                        workItemId,
                        user
                );

        return ApiResponse.success(
                "Work item details fetched successfully",
                response
        );
    }

    @GetMapping("/focus")
    public ApiResponse<MyFocusResponse> fetchFocus(
            Authentication authentication
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        MyFocusResponse response =
                mySummaryService.fetchFocus(user);

        return ApiResponse.success(
                "My focus information fetched successfully",
                response
        );
    }

    @GetMapping("/activities")
    public ApiResponse<List<MyActivityResponse>> fetchActivities(
            Authentication authentication,
            @RequestParam(
                    name = "limit",
                    defaultValue = "10"
            ) int limit
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        List<MyActivityResponse> response =
                mySummaryService.fetchActivities(
                        user,
                        limit
                );

        return ApiResponse.success(
                "My recent activities fetched successfully",
                response
        );
    }

    @GetMapping("/audits")
    public ApiResponse<List<MyAuditResponse>> fetchAudits(
            Authentication authentication,
            @RequestParam(
                    name = "limit",
                    defaultValue = "10"
            ) int limit
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        List<MyAuditResponse> response =
                mySummaryService.fetchAudits(
                        user,
                        limit
                );

        return ApiResponse.success(
                "My recent audit events fetched successfully",
                response
        );
    }
}