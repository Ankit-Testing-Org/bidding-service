package com.evatech.bidplatform.approval.service;

import com.evatech.bidplatform.approval.entity.workflow.WorkflowAction;
import com.evatech.bidplatform.approval.entity.workflow.WorkflowInstance;
import com.evatech.bidplatform.approval.entity.workflow.WorkflowTask;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.user.entity.User;

public interface ApprovalWorkflowService {

    ContractDocument submitForReview(
            Long contractId,
            String userId,
            User user);

    WorkflowInstance startWorkflow(
            String workflowKey,
            String businessType,
            Long businessId,
            String startedBy
    );

    WorkflowTask assignTaskToMe(Long taskId, String userId);

    WorkflowTask submitTask(Long taskId, WorkflowAction action, String userId, String comment);
}