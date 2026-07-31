package com.evatech.bidplatform.approval.dto;

import com.evatech.bidplatform.approval.entity.workflow.WorkflowAction;

public record SubmitWorkflowTaskRequest(
        WorkflowAction action,
        String comment
) {
}