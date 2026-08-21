package com.evatech.bidplatform.dashboard.service;

import com.evatech.bidplatform.dashboard.dto.approval.task.request.ApprovalTaskDecisionRequest;
import com.evatech.bidplatform.dashboard.dto.approval.task.request.ClarificationRequest;
import com.evatech.bidplatform.dashboard.dto.approval.task.request.ReassignApprovalTaskRequest;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.ApprovalDecisionResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.ApprovalTaskWorkspaceResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.AssignApprovalTaskResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.ClarificationResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.EligibleApproverResponse;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.ReassignApprovalTaskResponse;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ApprovalTaskWorkspaceService {

    ApprovalTaskWorkspaceResponse fetchWorkspace(
            String search,
            User user
    );

    AssignApprovalTaskResponse assignTaskToMe(
            Long taskId,
            User user
    );

    ApprovalDecisionResponse submitDecision(
            Long taskId,
            ApprovalTaskDecisionRequest request,
            User user
    );

    ClarificationResponse requestClarification(
            Long taskId,
            ClarificationRequest request,
            User user
    );

    ReassignApprovalTaskResponse reassignTask(
            Long taskId,
            ReassignApprovalTaskRequest request,
            User user
    );

    List<EligibleApproverResponse> fetchEligibleApprovers(
            Long taskId,
            User user
    );
}