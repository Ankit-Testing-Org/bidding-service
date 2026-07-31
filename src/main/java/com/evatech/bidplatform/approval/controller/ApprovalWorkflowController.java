package com.evatech.bidplatform.approval.controller;


import com.evatech.bidplatform.approval.dto.SubmitWorkflowTaskRequest;
import com.evatech.bidplatform.approval.entity.workflow.WorkflowTask;
import com.evatech.bidplatform.approval.service.ApprovalWorkflowService;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
public class ApprovalWorkflowController {

    private final ApprovalWorkflowService approvalWorkflowService;

    public ApprovalWorkflowController(ApprovalWorkflowService approvalWorkflowService) {
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @PostMapping("/{contractId}/submit-for-review")
    public ContractDocument submitForReview(
            @PathVariable Long contractId,
            Authentication authentication
    ) {
        return approvalWorkflowService.submitForReview(
                contractId,
                authentication.getName()
        );
    }

    @PostMapping("/tasks/{taskId}/assign-to-me")
    public WorkflowTask assignToMe(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
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
        return approvalWorkflowService.submitTask(
                taskId,
                request.action(),
                authentication.getName(),
                request.comment()
        );
    }
}