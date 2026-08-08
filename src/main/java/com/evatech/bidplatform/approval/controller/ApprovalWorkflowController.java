package com.evatech.bidplatform.approval.controller;


import com.evatech.bidplatform.approval.dto.SubmitWorkflowTaskRequest;
import com.evatech.bidplatform.approval.entity.workflow.WorkflowTask;
import com.evatech.bidplatform.approval.service.ApprovalWorkflowService;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
            @PathVariable Long contractId,
            Authentication authentication
    ) {
        User user = authenticateAndFetchUser(userRepo, authentication);
        List<String> roles = fetchRolesForUser(authentication);

        return approvalWorkflowService.submitForReview(
                contractId,
                authentication.getName(),
                user,
                roles
        );
    }

    @PostMapping("/tasks/{taskId}/assign-to-me")
    public WorkflowTask assignToMe(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        authenticateAndFetchUser(userRepo, authentication);
        return approvalWorkflowService.assignTaskToMe(
                taskId,
                authentication.getName()
        );
    }

    @PostMapping("/tasks/{taskId}/submit")
    public WorkflowTask submitTask(
            @PathVariable Long taskId,
            @RequestBody SubmitWorkflowTaskRequest request,
            Authentication authentication
    ) {
        authenticateAndFetchUser(userRepo, authentication);
        return approvalWorkflowService.submitTask(
                taskId,
                request.action(),
                authentication.getName(),
                request.comment()
        );
    }
}