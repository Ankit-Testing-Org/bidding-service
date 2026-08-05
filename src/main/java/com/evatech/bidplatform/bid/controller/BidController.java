package com.evatech.bidplatform.bid.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidField;
import com.evatech.bidplatform.bid.service.AiBidFormFillPersistenceService;
import com.evatech.bidplatform.bid.service.BidService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.exception.CustomException;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;
    private final AiBidFormFillPersistenceService aiBidFormFillPersistenceService;
    private final UserRepository userRepo;

    @PostMapping
    public ApiResponse<Bid> createBid(
            Authentication authentication,
            @RequestParam Long contractId,
            @RequestParam String title,
            @RequestParam String createdBy
    ) {
        User user =  authenticateAndFetchUser(authentication);
        Bid bid = bidService.createBid(contractId, title, createdBy, user.getUserName());

        return ApiResponse.success(
                "Bid created successfully",
                bid
        );
    }

    @GetMapping("/{bidId}")
    public ApiResponse<Bid> getBid(
            Authentication authentication,
            @PathVariable Long bidId
    ) {
        User user =  authenticateAndFetchUser(authentication);
        Bid bid = bidService.getBid(bidId, user.getUserName());

        return ApiResponse.success(
                "Bid fetched successfully",
                bid
        );
    }

    @GetMapping("/{bidId}/fields")
    public ApiResponse<List<BidField>> getBidFields(
            Authentication authentication,
            @PathVariable Long bidId
    ) {
        User user =  authenticateAndFetchUser(authentication);
        List<BidField> fields = bidService.getBidFields(bidId, user.getUserName());
        return ApiResponse.success(
                "Bid fields fetched successfully",
                fields
        );
    }

    @PostMapping("/{bidId}/fill-form")
    public ApiResponse<List<BidField>> fillBidFormUsingAi(
            Authentication authentication,
            @PathVariable Long bidId
    ) {
        User user =  authenticateAndFetchUser(authentication);
        List<BidField> fields = aiBidFormFillPersistenceService.fillBidFormUsingAi(bidId, user.getUserName());

        return ApiResponse.success(
                "Bid form filled using AI",
                fields
        );
    }

    @PutMapping("/{bidId}/fields")
    public ApiResponse<Bid> updateBidFields(
            Authentication authentication,
            @PathVariable Long bidId,
            @RequestBody Map<String, String> fields
    ) {
        User user =  authenticateAndFetchUser(authentication);
        Bid bid = bidService.updateBidFields(bidId, fields, user.getUserName());

        return ApiResponse.success(
                "Bid fields updated successfully",
                bid
        );
    }

    @PostMapping("/{bidId}/submit")
    public ApiResponse<Bid> submitBid(
            Authentication authentication,
            @PathVariable Long bidId
    ) {
        User user =  authenticateAndFetchUser(authentication);
        Bid bid = bidService.submitBid(bidId, user.getUserName());

        return ApiResponse.success(
                "Bid submitted for approval successfully",
                bid
        );
    }

    private User authenticateAndFetchUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();

        String keycloakUserId = jwt.getSubject(); // UUID

        return userRepo.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new CustomException("User not found"));
    }
}