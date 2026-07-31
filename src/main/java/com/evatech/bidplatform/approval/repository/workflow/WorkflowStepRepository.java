package com.evatech.bidplatform.approval.repository.workflow;

import com.evatech.bidplatform.approval.entity.workflow.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {

    Optional<WorkflowStep> findFirstByWorkflowDefinitionIdOrderByStepOrderAsc(Long workflowDefinitionId);
    Optional<WorkflowStep> findByWorkflowDefinitionIdAndStepKey(Long workflowDefinitionId, String stepKey);
}