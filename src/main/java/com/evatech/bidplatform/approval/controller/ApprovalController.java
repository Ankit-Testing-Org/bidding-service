package com.evatech.bidplatform.approval.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.approval.entity.ApprovalHistory;
import com.evatech.bidplatform.approval.entity.ApprovalTask;
import com.evatech.bidplatform.approval.service.ApprovalService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.exception.CustomException;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController extends AbstractController {

    private final ApprovalService approvalService;
    private final UserRepository userRepo;

    @GetMapping("/my-tasks")
    public ApiResponse<List<ApprovalTask>> getMyApprovalTasks(
            Authentication authentication) {
        User user = authenticateAndFetchUser(userRepo, authentication);
        List<ApprovalTask> tasks = approvalService.getMyApprovalTasks(user.getEmail());

        return ApiResponse.success(
                "Approval tasks fetched successfully",
                tasks
        );
    }

    @PreAuthorize("hasRole('REVIEWER')")
    @PostMapping("/{taskId}/approve")
    public ApiResponse<ApprovalTask> approve(
            Authentication authentication,
            @PathVariable Long taskId,
            @RequestParam(required = false) String comment
    ) {
        User user = authenticateAndFetchUser(userRepo, authentication);
        ApprovalTask task = approvalService.approve(taskId, user.getEmail(), comment);

        return ApiResponse.success(
                "Approval task approved successfully",
                task
        );
    }

    @PreAuthorize("hasRole('REVIEWER')")
    @PostMapping("/{taskId}/reject")
    public ApiResponse<ApprovalTask> reject(
            Authentication authentication,
            @PathVariable Long taskId,
            @RequestParam(required = false) String comment
    ) {
        User user = authenticateAndFetchUser(userRepo, authentication);
        ApprovalTask task = approvalService.reject(taskId, user.getEmail(), comment);

        return ApiResponse.success(
                "Approval task rejected successfully",
                task
        );
    }

    @PreAuthorize("hasRole('REVIEWER')")
    @PostMapping("/{taskId}/request-changes")
    public ApiResponse<ApprovalTask> requestChanges(
            Authentication authentication,
            @PathVariable Long taskId,
            @RequestParam String comment
    ) {
        User user = authenticateAndFetchUser(userRepo, authentication);
        ApprovalTask task = approvalService.requestChanges(taskId, user.getEmail(), comment);
        return ApiResponse.success(
                "Changes requested successfully",
                task
        );
    }

    @GetMapping("/bids/{bidId}/history")
    public ApiResponse<List<ApprovalHistory>> getApprovalHistory(
            Authentication authentication,
            @PathVariable Long bidId
    ) {
        authenticateAndFetchUser(userRepo, authentication);
        List<ApprovalHistory> history = approvalService.getApprovalHistory(bidId);
        return ApiResponse.success(
                "Approval history fetched successfully",
                history
        );
    }
}