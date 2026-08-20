package com.evatech.bidplatform.dashboard.service;

import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.review.request.ReviewDecisionRequest;
import com.evatech.bidplatform.dashboard.dto.review.request.ReviewSearchRequest;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewDashboardResponse;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewDecisionResponse;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewDetailResponse;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewResponse;
import com.evatech.bidplatform.user.entity.User;

public interface ReviewDashboardService {

    ReviewDashboardResponse fetchReviewDashboard(User user);

    PageResponse<ReviewResponse> searchReview(ReviewSearchRequest request, User user);

    ReviewDetailResponse fetchReview(Long reviewId, User user);

    ReviewDecisionResponse fetchReviewDecisionByReviewId(Long reviewId, ReviewDecisionRequest request, User user);
}
