package com.evatech.bidplatform.dashboard.service;

import com.evatech.bidplatform.dashboard.dto.approval.request.ApprovalDecisionRequest;
import com.evatech.bidplatform.dashboard.dto.approval.request.ApprovalSearchRequest;
import com.evatech.bidplatform.dashboard.dto.approval.response.*;
import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ApprovalDashboardService {

    ApprovalDashboardResponse fetchApprovalDashboard(User user);

    PageResponse<ApprovalResponse> searchApprovals(ApprovalSearchRequest request,
                                                  User user);

    ApprovalDetailResponse fetchApproval(Long approvalId,
                                             User user);

    ApprovalDecisionResponse submitDecision(Long approvalId,
                                                       ApprovalDecisionRequest request,
                                                       User user);

    List<DelegateUserResponse> fetchEligibleDelegates(Long approvalId, User user);
}
