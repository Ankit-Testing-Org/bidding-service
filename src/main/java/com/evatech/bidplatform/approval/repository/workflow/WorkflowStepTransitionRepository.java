package com.evatech.bidplatform.approval.repository.workflow;

import com.evatech.bidplatform.approval.entity.workflow.WorkflowAction;
import com.evatech.bidplatform.approval.entity.workflow.WorkflowStepTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowStepTransitionRepository extends JpaRepository<WorkflowStepTransition, Long> {
    Optional<WorkflowStepTransition> findByFromStepIdAndAction(Long fromStepId, WorkflowAction action);
}