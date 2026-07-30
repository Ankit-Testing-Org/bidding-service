package com.evatech.bidplatform.approval.repository;

import com.evatech.bidplatform.approval.entity.ApprovalHistory;
import com.evatech.bidplatform.approval.entity.ApprovalStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {

    List<ApprovalHistory> findByBidId(Long bidId);

    List<ApprovalHistory> findByBidIdOrderByActionAtAsc(Long bidId);

    List<ApprovalHistory> findByActionBy(String actionBy);

    List<ApprovalHistory> findByBidIdAndStage(
            Long bidId,
            ApprovalStage stage
    );
}