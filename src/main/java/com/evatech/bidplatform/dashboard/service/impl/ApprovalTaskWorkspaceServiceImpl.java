package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.approval.task.request.ApprovalTaskDecisionRequest;
import com.evatech.bidplatform.dashboard.dto.approval.task.request.ClarificationRequest;
import com.evatech.bidplatform.dashboard.dto.approval.task.request.ReassignApprovalTaskRequest;
import com.evatech.bidplatform.dashboard.dto.approval.task.response.*;
import com.evatech.bidplatform.dashboard.service.ApprovalTaskWorkspaceService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class ApprovalTaskWorkspaceServiceImpl implements ApprovalTaskWorkspaceService {

    @Override
    public ApprovalTaskWorkspaceResponse fetchWorkspace(String search, User user) {
        return null;
    }

    @Override
    public AssignApprovalTaskResponse assignTaskToMe(Long taskId, User user) {
        return null;
    }

    @Override
    public ApprovalDecisionResponse submitDecision(Long taskId, ApprovalTaskDecisionRequest request, User user) {
        return null;
    }

    @Override
    public ClarificationResponse requestClarification(Long taskId, ClarificationRequest request, User user) {
        return null;
    }

    @Override
    public ReassignApprovalTaskResponse reassignTask(Long taskId, ReassignApprovalTaskRequest request, User user) {
        return null;
    }

    @Override
    public List<EligibleApproverResponse> fetchEligibleApprovers(Long taskId, User user) {
        return List.of();
    }
}
