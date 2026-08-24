package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.bid.entity.GeneratedDocument;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;


public interface BidTemplateService {

    GeneratedDocument generateBidTemplate(
            Long contractId,
            User user,
            List<String> roles
    );
}