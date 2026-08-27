package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.common.controller.AbstractController;
import com.evatech.bidplatform.dashboard.dto.proposal.response.PageResponse;
import com.evatech.bidplatform.dashboard.dto.review.request.ReviewDecisionRequest;
import com.evatech.bidplatform.dashboard.dto.review.request.ReviewSearchRequest;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewDashboardResponse;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewDecisionResponse;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewDetailResponse;
import com.evatech.bidplatform.dashboard.dto.review.response.ReviewResponse;
import com.evatech.bidplatform.dashboard.service.ReviewDashboardService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewDashboardController extends AbstractController {

    private final UserRepository userRepository;
    private final ReviewDashboardService reviewDashboardService;

    @GetMapping("/dashboard")
    public ApiResponse<ReviewDashboardResponse> fetchDashboard() {
        User user = authenticateAndFetchUser(userRepository);

        ReviewDashboardResponse response = reviewDashboardService.fetchReviewDashboard(user);

        return ApiResponse.success("Review dashboard data fetched successfully",
                response);
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse<ReviewResponse>> searchReview(@RequestBody @Valid ReviewSearchRequest request) {
        User user = authenticateAndFetchUser(userRepository);

        PageResponse<ReviewResponse> response = reviewDashboardService.searchReview(request, user);

        return ApiResponse.success("Review response returned successfully",
                response);
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewDetailResponse> fetchReview(@PathVariable("reviewId") Long reviewId) {
        User user =  authenticateAndFetchUser(userRepository);

        ReviewDetailResponse response = reviewDashboardService.fetchReview(reviewId, user);
        return ApiResponse.success("Review data fetched successfully",
                response);
    }

    @PostMapping("/{reviewId}/decision")
    public ApiResponse<ReviewDecisionResponse> fetchReviewDecisionByReviewId(
            @PathVariable("reviewId") Long reviewId,
            @RequestBody @Valid ReviewDecisionRequest request
    ) {
        User user = authenticateAndFetchUser(userRepository);

        ReviewDecisionResponse response =
                reviewDashboardService.fetchReviewDecisionByReviewId(
                        reviewId, request, user);

        return ApiResponse.success(
                "Review response returned successfully", response);
    }
}
