package com.evatech.bidplatform.approval.repository.workflow;

import com.evatech.bidplatform.approval.entity.workflow.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    Optional<WorkflowInstance> findByBusinessTypeAndBusinessId(String businessType, Long businessId);
}