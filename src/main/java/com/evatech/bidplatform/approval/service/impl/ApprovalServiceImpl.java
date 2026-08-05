package com.evatech.bidplatform.approval.service.impl;

import com.evatech.bidplatform.approval.entity.ApprovalHistory;
import com.evatech.bidplatform.approval.entity.ApprovalStage;
import com.evatech.bidplatform.approval.entity.ApprovalStatus;
import com.evatech.bidplatform.approval.entity.ApprovalTask;
import com.evatech.bidplatform.approval.repository.ApprovalHistoryRepository;
import com.evatech.bidplatform.approval.repository.ApprovalTaskRepository;
import com.evatech.bidplatform.approval.service.ApprovalService;
import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidStatus;
import com.evatech.bidplatform.bid.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalTaskRepository approvalTaskRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final BidRepository bidRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalTask> getMyApprovalTasks(String approver) {
        return approvalTaskRepository.findByAssignedToAndStatus(
                approver,
                ApprovalStatus.PENDING
        );
    }

    @Override
    public ApprovalTask approve(Long taskId, String approver, String comment) {
        ApprovalTask task = approvalTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Approval task not found with id: " + taskId));

        if (!task.isPending()) {
            throw new IllegalStateException("Only pending approval task can be approved");
        }

        if (!task.getAssignedTo().equalsIgnoreCase(approver)) {
            throw new IllegalStateException("Approver is not assigned to this task");
        }

        task.approve(comment);
        approvalTaskRepository.save(task);

        saveHistory(task.getBid(), approver, ApprovalStatus.APPROVED.name(), task.getStage(), comment);

        createNextApprovalTaskOrApproveBid(task.getBid(), task.getStage());

        return task;
    }

    @Override
    public ApprovalTask reject(Long taskId, String approver, String comment) {
        ApprovalTask task = approvalTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Approval task not found with id: " + taskId));

        if (!task.isPending()) {
            throw new IllegalStateException("Only pending approval task can be rejected");
        }

        task.reject(comment);
        approvalTaskRepository.save(task);

        Bid bid = task.getBid();
        bid.setStatus(BidStatus.REJECTED);
        bidRepository.save(bid);

        saveHistory(bid, approver, ApprovalStatus.REJECTED.name(), task.getStage(), comment);

        return task;
    }

    @Override
    @Transactional
    public ApprovalTask requestChanges(
            Long taskId,
            String approver,
            String comment
    ) {

        ApprovalTask task = approvalTaskRepository.findById(taskId).orElseThrow();

        Bid bid = task.getBid();

        task.setStatus(ApprovalStatus.CHANGES_REQUESTED);
        task.setComment(comment);
        task.setCompletedAt(LocalDateTime.now());
        task.setAssignedTo(bid.getSubmittedBy());
        ApprovalTask approvalTask = approvalTaskRepository.save(task);

        saveHistory(task.getBid(), approver, ApprovalStatus.CHANGES_REQUESTED.name(), task.getStage(), comment);

        return approvalTask;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalHistory> getApprovalHistory(Long bidId) {
        return approvalHistoryRepository.findByBidIdOrderByActionAtAsc(bidId);
    }

    private void saveHistory(
            Bid bid,
            String actionBy,
            String action,
            ApprovalStage stage,
            String comment
    ) {
        ApprovalHistory history = ApprovalHistory.builder()
                .bid(bid)
                .actionBy(actionBy)
                .action(action)
                .stage(stage)
                .comment(comment)
                .actionAt(LocalDateTime.now())
                .build();

        approvalHistoryRepository.save(history);
    }

    private void createNextApprovalTaskOrApproveBid(Bid bid, ApprovalStage currentStage) {
        ApprovalStage nextStage = getNextStage(currentStage);

        if (nextStage == null) {
            bid.setStatus(BidStatus.APPROVED);
            bidRepository.save(bid);
            return;
        }

        ApprovalTask nextTask = ApprovalTask.builder()
                .bid(bid)
                .stage(nextStage)
                .assignedTo(resolveApprover(nextStage))
                .status(ApprovalStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        approvalTaskRepository.save(nextTask);
    }

    private ApprovalStage getNextStage(ApprovalStage currentStage) {
        return switch (currentStage) {
            case MANAGER -> ApprovalStage.FINANCE;
            case FINANCE -> ApprovalStage.LEGAL;
            case LEGAL -> ApprovalStage.COMMERCIAL;
            case COMMERCIAL -> ApprovalStage.FINAL;
            case FINAL -> null;
        };
    }

    private String resolveApprover(ApprovalStage stage) {
        return switch (stage) {
            case MANAGER -> "manager";
            case FINANCE -> "finance";
            case LEGAL -> "legal";
            case COMMERCIAL -> "commercial";
            case FINAL -> "final-approver";
        };
    }
}