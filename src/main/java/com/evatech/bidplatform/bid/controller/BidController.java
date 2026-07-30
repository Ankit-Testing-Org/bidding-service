package com.evatech.bidplatform.bid.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidField;
import com.evatech.bidplatform.bid.service.AiBidFormFillPersistenceService;
import com.evatech.bidplatform.bid.service.BidService;
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
public class BidController {

    private final BidService bidService;
    private final AiBidFormFillPersistenceService aiBidFormFillPersistenceService;

    public BidController(
            BidService bidService,
            AiBidFormFillPersistenceService aiBidFormFillPersistenceService
    ) {
        this.bidService = bidService;
        this.aiBidFormFillPersistenceService = aiBidFormFillPersistenceService;
    }

    @PostMapping
    public ApiResponse<Bid> createBid(
            @RequestParam Long contractId,
            @RequestParam String title,
            @RequestParam String createdBy
    ) {
        Bid bid = bidService.createBid(contractId, title, createdBy);

        return ApiResponse.success(
                "Bid created successfully",
                bid
        );
    }

    @GetMapping("/{bidId}")
    public ApiResponse<Bid> getBid(
            @PathVariable Long bidId
    ) {
        Bid bid = bidService.getBid(bidId);

        return ApiResponse.success(
                "Bid fetched successfully",
                bid
        );
    }

    @GetMapping("/{bidId}/fields")
    public ApiResponse<List<BidField>> getBidFields(
            @PathVariable Long bidId
    ) {
        List<BidField> fields = bidService.getBidFields(bidId);

        return ApiResponse.success(
                "Bid fields fetched successfully",
                fields
        );
    }

    @PostMapping("/{bidId}/fill-form")
    public ApiResponse<List<BidField>> fillBidFormUsingAi(
            @PathVariable Long bidId
    ) {
        List<BidField> fields = aiBidFormFillPersistenceService.fillBidFormUsingAi(bidId);

        return ApiResponse.success(
                "Bid form filled using AI",
                fields
        );
    }

    @PutMapping("/{bidId}/fields")
    public ApiResponse<Bid> updateBidFields(
            @PathVariable Long bidId,
            @RequestBody Map<String, String> fields
    ) {
        Bid bid = bidService.updateBidFields(bidId, fields);

        return ApiResponse.success(
                "Bid fields updated successfully",
                bid
        );
    }

    @PostMapping("/{bidId}/submit")
    public ApiResponse<Bid> submitBid(
            @PathVariable Long bidId
    ) {
        Bid bid = bidService.submitBid(bidId);

        return ApiResponse.success(
                "Bid submitted for approval successfully",
                bid
        );
    }
}