package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.common.controller.AbstractController;
import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.approval.request.ApprovalDecisionRequest;
import com.evatech.bidplatform.dashboard.dto.approval.request.ApprovalSearchRequest;
import com.evatech.bidplatform.dashboard.dto.approval.response.ApprovalDashboardResponse;
import com.evatech.bidplatform.dashboard.dto.approval.response.ApprovalDecisionResponse;
import com.evatech.bidplatform.dashboard.dto.approval.response.ApprovalDetailResponse;
import com.evatech.bidplatform.dashboard.dto.approval.response.ApprovalResponse;
import com.evatech.bidplatform.dashboard.dto.approval.response.DelegateUserResponse;
import com.evatech.bidplatform.dashboard.service.ApprovalDashboardService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalDashboardController extends AbstractController {

    private final UserRepository userRepository;
    private final ApprovalDashboardService approvalDashboardService;

    @GetMapping("/dashboard")
    public ApiResponse<ApprovalDashboardResponse> fetchDashboard() {
        User user = authenticateAndFetchUser(
                userRepository);

        ApprovalDashboardResponse response =
                approvalDashboardService.fetchApprovalDashboard(user);

        return ApiResponse.success(
                "Approval dashboard data fetched successfully",
                response
        );
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse<ApprovalResponse>> searchApprovals(
            @RequestBody @Valid ApprovalSearchRequest request
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        PageResponse<ApprovalResponse> response =
                approvalDashboardService.searchApprovals(
                        request,
                        user
                );

        return ApiResponse.success(
                "Approval tasks returned successfully",
                response
        );
    }

    @GetMapping("/{approvalId}")
    public ApiResponse<ApprovalDetailResponse> fetchApproval(
            @PathVariable("approvalId") Long approvalId
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        ApprovalDetailResponse response =
                approvalDashboardService.fetchApproval(
                        approvalId,
                        user
                );

        return ApiResponse.success(
                "Approval data fetched successfully",
                response
        );
    }

    @PostMapping("/{approvalId}/decision")
    public ApiResponse<ApprovalDecisionResponse> submitDecision(
            @PathVariable("approvalId") Long approvalId,
            @RequestBody @Valid ApprovalDecisionRequest request
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        ApprovalDecisionResponse response =
                approvalDashboardService.submitDecision(
                        approvalId,
                        request,
                        user
                );

        return ApiResponse.success(
                "Approval decision submitted successfully",
                response
        );
    }

    @GetMapping("/{approvalId}/delegates")
    public ApiResponse<List<DelegateUserResponse>> fetchDelegates(
            @PathVariable("approvalId") Long approvalId
    ) {
        User user = authenticateAndFetchUser(
                userRepository);

        List<DelegateUserResponse> response =
                approvalDashboardService.fetchEligibleDelegates(
                        approvalId,
                        user
                );

        return ApiResponse.success(
                "Eligible delegate users fetched successfully",
                response
        );
    }
}