package com.evatech.bidplatform.approval.repository;

import com.evatech.bidplatform.approval.entity.ApprovalStatus;
import com.evatech.bidplatform.approval.entity.ApprovalTask;
import com.evatech.bidplatform.user.dto.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalTaskRepository
        extends JpaRepository<ApprovalTask, Long> {

    List<ApprovalTask> findByAssignedToAndStatus(
            String assignedTo,
            ApprovalStatus status);

    Long countByStatusAndAssignedRole(
            ApprovalStatus approvalStatus,
            RoleType roleType);
}
