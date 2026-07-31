package com.evatech.bidplatform.approval.repository.workflow;

import com.evatech.bidplatform.approval.entity.workflow.WorkflowTask;
import com.evatech.bidplatform.approval.entity.workflow.WorkflowTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {

    Optional<WorkflowTask> findFirstByWorkflowInstanceIdAndStatusInOrderByCreatedAtDesc(
            Long workflowInstanceId,
            Collection<WorkflowTaskStatus> statuses
    );

    List<WorkflowTask> findByAssigneeGroupKeyAndStatus(
            String assigneeGroupKey,
            WorkflowTaskStatus status
    );

    List<WorkflowTask> findByAssigneeUserIdAndStatus(
            String assigneeUserId,
            WorkflowTaskStatus status
    );
}