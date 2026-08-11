package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.bid.dto.request.AssignBidRequest;
import com.evatech.bidplatform.bid.dto.response.BidTemplateFieldUpdateResponse;
import com.evatech.bidplatform.bid.dto.response.DeleteDocumentResponse;
import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidDocument;
import com.evatech.bidplatform.bid.entity.BidDocumentType;
import com.evatech.bidplatform.user.entity.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BidService {

    Bid assignBid(
            Long contractId,
            AssignBidRequest request,
            User user,
            List<String> roles);

    BidDocument uploadDocument(
            Long bidId,
            MultipartFile file,
            BidDocumentType documentType,
            User user);

    BidDocument uploadCompletedBid(
            Long bidId,
            MultipartFile file,
            User user,
            List<String> roles);

    Resource downloadDocument(Long documentId, User user, List<String> roles);

    DeleteDocumentResponse deleteDocument(Long documentId, User user, List<String> roles);

    List<BidTemplateFieldUpdateResponse> uploadTemplateToContract(
            Long bidId,
            Long templateId,
            Long contractId,
            User user,
            List<String> roles);
}
