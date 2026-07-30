package com.evatech.bidplatform.approval.service;

import com.evatech.bidplatform.approval.entity.ApprovalHistory;
import com.evatech.bidplatform.approval.entity.ApprovalTask;

import java.util.List;

public interface ApprovalService {

    List<ApprovalTask> getMyApprovalTasks(String approver);
    ApprovalTask approve(Long taskId, String approver, String comment);
    ApprovalTask reject(Long taskId, String approver, String comment);
    List<ApprovalHistory> getApprovalHistory(Long bidId);
}
