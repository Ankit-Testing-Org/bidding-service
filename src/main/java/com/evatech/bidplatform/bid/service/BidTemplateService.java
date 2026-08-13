package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.bid.entity.GeneratedDocument;
import com.evatech.bidplatform.user.entity.User;
import org.springframework.core.io.Resource;

import java.util.List;

public interface BidTemplateService {

     GeneratedDocument generateBidTemplate(
            Long contractId,
            User user,
            List<String> roles);

    Resource downloadDocument(Long documentId, Long contractId, User user, List<String> roles);
}
