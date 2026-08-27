package com.evatech.bidplatform.approval.controller;


import com.evatech.bidplatform.approval.dto.SubmitWorkflowTaskRequest;
import com.evatech.bidplatform.approval.entity.workflow.WorkflowTask;
import com.evatech.bidplatform.approval.service.ApprovalWorkflowService;
import com.evatech.bidplatform.common.controller.AbstractController;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class ApprovalWorkflowController extends AbstractController {

    private final ApprovalWorkflowService approvalWorkflowService;
    private final UserRepository userRepo;

    @PostMapping("/{contractId}/submit-for-review")
    public ContractDocument submitForReview(
            @PathVariable Long contractId) {
        User user = authenticateAndFetchUser(userRepo);
        List<String> roles = fetchRolesForUser();

        return approvalWorkflowService.submitForReview(
                contractId,
                user.getEmail(),
                user,
                roles
        );
    }

    @PostMapping("/tasks/{taskId}/assign-to-me")
    public WorkflowTask assignToMe(
            @PathVariable Long taskId) {
        User user = authenticateAndFetchUser(userRepo);
        return approvalWorkflowService.assignTaskToMe(
                taskId,
                user.getEmail()
        );
    }

    @PostMapping("/tasks/{taskId}/submit")
    public WorkflowTask submitTask(
            @PathVariable Long taskId,
            @RequestBody SubmitWorkflowTaskRequest request) {
        User user = authenticateAndFetchUser(userRepo);
        return approvalWorkflowService.submitTask(
                taskId,
                request.action(),
                user.getEmail(),
                request.comment()
        );
    }
}