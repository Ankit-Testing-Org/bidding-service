package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.summary.request.MyWorkItemSearchRequest;
import com.evatech.bidplatform.dashboard.dto.summary.response.*;
import com.evatech.bidplatform.dashboard.service.MySummaryService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MySummaryServiceImpl implements MySummaryService {

    @Override
    public MySummaryDashboardResponse fetchDashboard(User user) {
        return null;
    }

    @Override
    public PageResponse<MyWorkItemResponse> searchWorkItems(MyWorkItemSearchRequest request, User user) {
        return null;
    }

    @Override
    public MyWorkItemDetailResponse fetchWorkItem(Long workItemId, User user) {
        return null;
    }

    @Override
    public MyFocusResponse fetchFocus(User user) {
        return null;
    }

    @Override
    public List<MyActivityResponse> fetchActivities(User user, int limit) {
        return List.of();
    }

    @Override
    public List<MyAuditResponse> fetchAudits(User user, int limit) {
        return List.of();
    }
}
