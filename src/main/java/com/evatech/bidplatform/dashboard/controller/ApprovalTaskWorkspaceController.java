package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.request.ApprovalTaskDecisionRequest;
import com.evatech.bidplatform.dashboard.dto.approval.task.request.ClarificationRequest;
import com.evatech.bidplatform.dashboard.dto.approval.task.request.ReassignApprovalTaskRequest;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.ApprovalDecisionResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.ApprovalTaskWorkspaceResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.AssignApprovalTaskResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.ClarificationResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.EligibleApproverResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.ReassignApprovalTaskResponse;
import com.evatech.bidplatform.dashboard.service.ApprovalTaskWorkspaceService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval-tasks")
@RequiredArgsConstructor
@Validated
public class ApprovalTaskWorkspaceController extends AbstractController {

    private final UserRepository userRepository;
    private final ApprovalTaskWorkspaceService approvalTaskWorkspaceService;

    @GetMapping("/workspace")
    public ApiResponse<ApprovalTaskWorkspaceResponse> fetchWorkspace(
            Authentication authentication,
            @RequestParam("search")
            @NotBlank(message = "Contract ID, task ID, or contract number is required")
            String search
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        ApprovalTaskWorkspaceResponse response =
                approvalTaskWorkspaceService.fetchWorkspace(
                        search.trim(),
                        user
                );

        return ApiResponse.success(
                "Approval task workspace loaded successfully",
                response
        );
    }

    @PostMapping("/{taskId}/assign-to-me")
    public ApiResponse<AssignApprovalTaskResponse> assignTaskToMe(
            Authentication authentication,
            @PathVariable("taskId")
            @Positive(message = "Task ID must be positive")
            Long taskId
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        AssignApprovalTaskResponse response =
                approvalTaskWorkspaceService.assignTaskToMe(
                        taskId,
                        user
                );

        return ApiResponse.success(
                "Approval task assigned successfully",
                response
        );
    }

    @PostMapping("/{taskId}/decision")
    public ApiResponse<ApprovalDecisionResponse> submitDecision(
            Authentication authentication,
            @PathVariable("taskId")
            @Positive(message = "Task ID must be positive")
            Long taskId,
            @RequestBody @Valid ApprovalTaskDecisionRequest request
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        ApprovalDecisionResponse response =
                approvalTaskWorkspaceService.submitDecision(
                        taskId,
                        request,
                        user
                );

        return ApiResponse.success(
                "Approval decision submitted successfully",
                response
        );
    }

    @PostMapping("/{taskId}/clarification")
    public ApiResponse<ClarificationResponse> requestClarification(
            Authentication authentication,
            @PathVariable("taskId")
            @Positive(message = "Task ID must be positive")
            Long taskId,
            @RequestBody @Valid ClarificationRequest request
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        ClarificationResponse response =
                approvalTaskWorkspaceService.requestClarification(
                        taskId,
                        request,
                        user
                );

        return ApiResponse.success(
                "Clarification requested successfully",
                response
        );
    }

    @PostMapping("/{taskId}/reassign")
    public ApiResponse<ReassignApprovalTaskResponse> reassignTask(
            Authentication authentication,
            @PathVariable("taskId")
            @Positive(message = "Task ID must be positive")
            Long taskId,
            @RequestBody @Valid ReassignApprovalTaskRequest request
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        ReassignApprovalTaskResponse response =
                approvalTaskWorkspaceService.reassignTask(
                        taskId,
                        request,
                        user
                );

        return ApiResponse.success(
                "Approval task reassigned successfully",
                response
        );
    }

    @GetMapping("/{taskId}/eligible-users")
    public ApiResponse<List<EligibleApproverResponse>> fetchEligibleUsers(
            Authentication authentication,
            @PathVariable("taskId")
            @Positive(message = "Task ID must be positive")
            Long taskId
    ) {
        User user = authenticateAndFetchUser(
                userRepository,
                authentication
        );

        List<EligibleApproverResponse> response =
                approvalTaskWorkspaceService.fetchEligibleApprovers(
                        taskId,
                        user
                );

        return ApiResponse.success(
                "Eligible approvers loaded successfully",
                response
        );
    }
}