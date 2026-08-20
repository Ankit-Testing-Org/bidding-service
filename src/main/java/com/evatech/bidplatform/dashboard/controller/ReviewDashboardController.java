package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.ApiResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewDashboardController extends AbstractController {

    private final UserRepository userRepository;
    private final ReviewDashboardService reviewDashboardService;

    @GetMapping("/dashboard")
    public ApiResponse<ReviewDashboardResponse> fetchDashboard(Authentication authentication) {
        User user = authenticateAndFetchUser(userRepository, authentication);

        ReviewDashboardResponse response = reviewDashboardService.fetchReviewDashboard(user);

        return ApiResponse.success("Review dashboard data fetched successfully",
                response);
    }

    @PostMapping("/search")
    public ApiResponse<PageResponse<ReviewResponse>> searchReview(Authentication authentication,
                                                                  @RequestBody @Valid ReviewSearchRequest request) {
        User user = authenticateAndFetchUser(userRepository, authentication);

        PageResponse<ReviewResponse> response = reviewDashboardService.searchReview(request, user);

        return ApiResponse.success("Review response returned successfully",
                response);
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewDetailResponse> fetchReview(Authentication authentication,
                                                         @PathVariable("reviewId") Long reviewId) {
        User user =  authenticateAndFetchUser(userRepository, authentication);

        ReviewDetailResponse response = reviewDashboardService.fetchReview(reviewId, user);
        return ApiResponse.success("Review data fetched successfully",
                response);
    }

    @PostMapping("/{reviewId}/decision")
    public ApiResponse<ReviewDecisionResponse> fetchReviewDecisionByReviewId(
            Authentication authentication,
            @PathVariable("reviewId") Long reviewId,
            @RequestBody @Valid ReviewDecisionRequest request
    ) {
        User user = authenticateAndFetchUser(userRepository, authentication);

        ReviewDecisionResponse response =
                reviewDashboardService.fetchReviewDecisionByReviewId(
                        reviewId, request, user);

        return ApiResponse.success(
                "Review response returned successfully", response);
    }
}
