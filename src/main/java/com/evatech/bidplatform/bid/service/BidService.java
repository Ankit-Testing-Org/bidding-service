package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidField;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;
import java.util.Map;

public interface BidService {

    Bid createBid(
            Long contractId,
            String lotNumber,
            String title,
            String createdBy,
            User user,
            List<String> roles);

    Bid getBid(Long bidId,  User user);
    Bid updateBidFields(Long bidId, Map<String, String> fields, User user);
    Bid submitBid(Long bidId,  User user);
    List<BidField> fillBidFormUsingAi(Long bidId, User user);
    List<BidField> getBidFields(Long bidId,  User user);

}
