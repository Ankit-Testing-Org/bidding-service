package com.evatech.bidplatform.approval.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.approval.entity.ApprovalHistory;
import com.evatech.bidplatform.approval.entity.ApprovalTask;
import com.evatech.bidplatform.approval.service.ApprovalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(
            ApprovalService approvalService
    ) {
        this.approvalService = approvalService;
    }

    @GetMapping("/my-tasks")
    public ApiResponse<List<ApprovalTask>> getMyApprovalTasks(
            @RequestParam String approver
    ) {
        List<ApprovalTask> tasks = approvalService.getMyApprovalTasks(approver);

        return ApiResponse.success(
                "Approval tasks fetched successfully",
                tasks
        );
    }

    @PostMapping("/{taskId}/approve")
    public ApiResponse<ApprovalTask> approve(
            @PathVariable Long taskId,
            @RequestParam String approver,
            @RequestParam(required = false) String comment
    ) {
        ApprovalTask task = approvalService.approve(taskId, approver, comment);

        return ApiResponse.success(
                "Approval task approved successfully",
                task
        );
    }

    @PostMapping("/{taskId}/reject")
    public ApiResponse<ApprovalTask> reject(
            @PathVariable Long taskId,
            @RequestParam String approver,
            @RequestParam(required = false) String comment
    ) {
        ApprovalTask task = approvalService.reject(taskId, approver, comment);

        return ApiResponse.success(
                "Approval task rejected successfully",
                task
        );
    }

    @GetMapping("/bids/{bidId}/history")
    public ApiResponse<List<ApprovalHistory>> getApprovalHistory(
            @PathVariable Long bidId
    ) {
        List<ApprovalHistory> history = approvalService.getApprovalHistory(bidId);

        return ApiResponse.success(
                "Approval history fetched successfully",
                history
        );
    }
}