package com.evatech.bidplatform.approval.repository.workflow;

import com.evatech.bidplatform.approval.entity.workflow.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    Optional<WorkflowDefinition> findByWorkflowKeyAndActiveTrue(String workflowKey);
}