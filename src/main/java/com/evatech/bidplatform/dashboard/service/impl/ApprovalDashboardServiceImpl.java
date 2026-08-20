package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.approval.request.ApprovalDecisionRequest;
import com.evatech.bidplatform.dashboard.dto.approval.request.ApprovalSearchRequest;
import com.evatech.bidplatform.dashboard.dto.approval.response.*;
import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.service.ApprovalDashboardService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalDashboardServiceImpl implements ApprovalDashboardService  {

    @Override
    public ApprovalDashboardResponse fetchApprovalDashboard(User user) {
        return null;
    }

    @Override
    public PageResponse<ApprovalResponse> searchApprovals(ApprovalSearchRequest request, User user) {
        return null;
    }

    @Override
    public ApprovalDetailResponse fetchApproval(Long approvalId, User user) {
        return null;
    }

    @Override
    public ApprovalDecisionResponse submitDecision(Long approvalId, ApprovalDecisionRequest request, User user) {
        return null;
    }

    @Override
    public List<DelegateUserResponse> fetchEligibleDelegates(Long approvalId, User user) {
        return null;
    }
}
