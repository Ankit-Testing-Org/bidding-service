package com.evatech.bidplatform.approval.service;

import com.evatech.bidplatform.approval.entity.workflow.WorkflowInstance;
import com.evatech.bidplatform.approval.entity.workflow.WorkflowTask;

public interface WorkflowNotificationService {

    void notifyGroupForReview(WorkflowTask task);
    void notifyRequesterWorkflowCompleted(WorkflowInstance instance);
    void notifyRequesterWorkflowRejected(WorkflowInstance instance);
}
