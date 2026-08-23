package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.bid.entity.BidDocumentType;
import com.evatech.bidplatform.dashboard.dto.bid.request.GenerateBidDocumentRequest;
import com.evatech.bidplatform.dashboard.dto.bid.request.SubmitBidForApprovalRequest;
import com.evatech.bidplatform.dashboard.dto.bid.response.*;
import com.evatech.bidplatform.dashboard.service.BidPreparationService;
import com.evatech.bidplatform.user.entity.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class BidPreparationServiceImpl implements BidPreparationService {
    @Override
    public LatestBidDocumentResponse getLatestBidDocument(Long contractId, User user, List<String> roles) {
        return null;
    }

    @Override
    public GeneratedBidDocumentResponse generateBidDocument(Long contractId, GenerateBidDocumentRequest request, User user, List<String> roles) {
        return null;
    }

    @Override
    public BidDocumentDetailsResponse getDocumentDetails(Long contractId, Long documentId, User user, List<String> roles) {
        return null;
    }

    @Override
    public Resource downloadDocument(Long contractId, Long documentId, User user, List<String> roles) {
        return null;
    }

    @Override
    public Resource previewDocument(Long contractId, Long documentId, User user, List<String> roles) {
        return null;
    }

    @Override
    public UploadedBidDocumentResponse uploadCompletedBid(Long contractId, Long sourceGeneratedDocumentId, BidDocumentType documentType, MultipartFile file, User user, List<String> roles) {
        return null;
    }

    @Override
    public SubmitBidForApprovalResponse submitForApproval(Long contractId, SubmitBidForApprovalRequest request, User user, List<String> roles) {
        return null;
    }

    @Override
    public BidPreparationStatusResponse getPreparationStatus(Long contractId, User user, List<String> roles) {
        return null;
    }
}
