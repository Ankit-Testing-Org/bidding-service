package com.evatech.bidplatform.bid.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.bid.dto.response.BidFieldResponse;
import com.evatech.bidplatform.bid.dto.response.BidResponse;
import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidField;
import com.evatech.bidplatform.bid.mapper.BidFieldMapper;
import com.evatech.bidplatform.bid.mapper.BidMapper;
import com.evatech.bidplatform.bid.service.AiBidFormFillPersistenceService;
import com.evatech.bidplatform.bid.service.BidService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.exception.CustomException;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;
    private final AiBidFormFillPersistenceService aiBidFormFillPersistenceService;
    private final UserRepository userRepo;
    private final BidMapper bidMapper;
    private final BidFieldMapper bidFieldMapper;

    @PostMapping
    public ApiResponse<Bid> createBid(
            Authentication authentication,
            @RequestParam Long contractId,
            @RequestParam String lotNumber,
            @RequestParam String title,
            @RequestParam String createdBy
    ) {
        User user =  authenticateAndFetchUser(authentication);
        List<String> roles = fetchRolesForUser(authentication);
        Bid bid = bidService.createBid(contractId, lotNumber, title, createdBy, user, roles);

        return ApiResponse.success("Bid created successfully", bid);
    }

    @GetMapping("/{bidId}")
    public ApiResponse<BidResponse> getBid(
            Authentication authentication,
            @PathVariable Long bidId
    ) {
        User user =  authenticateAndFetchUser(authentication);
        Bid bid = bidService.getBid(bidId, user);

        return ApiResponse.success(
                "Bid fetched successfully",
                bidMapper.toResponse(bid)
        );
    }

    @GetMapping("/{bidId}/fields")
    public ApiResponse<List<BidFieldResponse>> getBidFields(
            Authentication authentication,
            @PathVariable Long bidId
    ) {
        User user =  authenticateAndFetchUser(authentication);
        List<BidField> fields = bidService.getBidFields(bidId, user);
        return ApiResponse.success(
                "Bid fields fetched successfully",
                bidFieldMapper.toResponses(fields)
        );
    }

    @PostMapping("/{bidId}/fill-form")
    public ApiResponse<List<BidFieldResponse>> fillBidFormUsingAi(
            Authentication authentication,
            @PathVariable Long bidId
    ) {
        User user =  authenticateAndFetchUser(authentication);
        List<BidField> fields = aiBidFormFillPersistenceService.fillBidFormUsingAi(bidId, user.getUserName());

        return ApiResponse.success(
                "Bid form filled using AI",
                bidFieldMapper.toResponses(fields)
        );
    }

    @PutMapping("/{bidId}/fields")
    public ApiResponse<BidResponse> updateBidFields(
            Authentication authentication,
            @PathVariable Long bidId,
            @RequestBody Map<String, String> fields
    ) {
        User user =  authenticateAndFetchUser(authentication);
        Bid bid = bidService.updateBidFields(bidId, fields, user);

        return ApiResponse.success(
                "Bid fields updated successfully",
                bidMapper.toResponse(bid)

        );
    }

    @PostMapping("/{bidId}/submit")
    public ApiResponse<BidResponse> submitBid(
            Authentication authentication,
            @PathVariable Long bidId
    ) {
        User user =  authenticateAndFetchUser(authentication);
        Bid bid = bidService.submitBid(bidId, user);

        return ApiResponse.success(
                "Bid submitted for approval successfully",
                bidMapper.toResponse(bid)
        );
    }

    private User authenticateAndFetchUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();

        String keycloakUserId = jwt.getSubject(); // UUID

        return userRepo.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new CustomException("User not found"));
    }

    protected List<String> fetchRolesForUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");
        log.info("Roles {"+roles.toString()+"}");
        return roles;
    }
}