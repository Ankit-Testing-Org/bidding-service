package com.evatech.bidplatform.approval.repository;

import com.evatech.bidplatform.approval.entity.ApprovalStage;
import com.evatech.bidplatform.approval.entity.ApprovalStatus;
import com.evatech.bidplatform.approval.entity.ApprovalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalTaskRepository extends JpaRepository<ApprovalTask, Long> {

    List<ApprovalTask> findByBidId(Long bidId);

    List<ApprovalTask> findByAssignedTo(String assignedTo);

    List<ApprovalTask> findByAssignedToAndStatus(
            String assignedTo,
            ApprovalStatus status
    );

    Optional<ApprovalTask> findByBidIdAndStageAndStatus(
            Long bidId,
            ApprovalStage stage,
            ApprovalStatus status
    );

    List<ApprovalTask> findByBidIdOrderByCreatedAtAsc(Long bidId);
}
