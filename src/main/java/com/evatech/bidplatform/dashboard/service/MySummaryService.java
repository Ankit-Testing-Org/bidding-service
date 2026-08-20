package com.evatech.bidplatform.dashboard.service;

import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.summary.request.MyWorkItemSearchRequest;
import com.evatech.bidplatform.dashboard.dto.summary.response.*;
import com.evatech.bidplatform.user.entity.User;
import jakarta.validation.Valid;

import java.util.List;

public interface MySummaryService {
    MySummaryDashboardResponse fetchDashboard(User user);

    PageResponse<MyWorkItemResponse> searchWorkItems(@Valid MyWorkItemSearchRequest request, User user);

    MyWorkItemDetailResponse fetchWorkItem(Long workItemId, User user);

    MyFocusResponse fetchFocus(User user);

    List<MyActivityResponse> fetchActivities(User user, int limit);

    List<MyAuditResponse> fetchAudits(User user, int limit);
}
