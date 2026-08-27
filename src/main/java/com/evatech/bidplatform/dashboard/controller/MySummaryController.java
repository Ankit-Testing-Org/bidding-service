package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.common.controller.AbstractController;
import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.summary.request.MyWorkItemSearchRequest;
import com.evatech.bidplatform.dashboard.dto.summary.response.*;
import com.evatech.bidplatform.dashboard.service.MySummaryService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-summary")
@RequiredArgsConstructor
public class MySummaryController extends AbstractController {

    private final UserRepository userRepository;
    private final MySummaryService mySummaryService;

    @GetMapping("/dashboard")
    public ApiResponse<MySummaryDashboardResponse> fetchDashboard() {
        User user = authenticateAndFetchUser(
                userRepository);

        MySummaryDashboardResponse response =
                mySummaryService.fetchDashboard(user);

        return ApiResponse.success(
                "My summary dashboard fetched successfully",
                response
        );
    }

    @PostMapping("/work-items/search")
    public ApiResponse<PageResponse<MyWorkItemResponse>> searchWorkItems(@RequestBody @Valid MyWorkItemSearchRequest request
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

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
            @PathVariable("workItemId") Long workItemId
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

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
    ) {
        User user = authenticateAndFetchUser(
                userRepository
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
            @RequestParam(
                    name = "limit",
                    defaultValue = "10"
            ) int limit
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

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
            @RequestParam(
                    name = "limit",
                    defaultValue = "10"
            ) int limit
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

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