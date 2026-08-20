package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.review.request.ReviewDecisionRequest;
import com.evatech.bidplatform.dashboard.dto.review.request.ReviewSearchRequest;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewDashboardResponse;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewDecisionResponse;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewDetailResponse;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewResponse;
import com.evatech.bidplatform.dashboard.service.ReviewDashboardService;
import com.evatech.bidplatform.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReviewDashboardServiceImpl implements ReviewDashboardService {

    @Override
    public ReviewDashboardResponse fetchReviewDashboard(User user) {
        return null;
    }

    @Override
    public PageResponse<ReviewResponse> searchReview(ReviewSearchRequest request, User user) {
        return null;
    }

    @Override
    public ReviewDetailResponse fetchReview(Long reviewId, User user) {
        return null;
    }

    @Override
    public ReviewDecisionResponse fetchReviewDecisionByReviewId(Long reviewId, ReviewDecisionRequest request, User user) {
        return null;
    }
}
