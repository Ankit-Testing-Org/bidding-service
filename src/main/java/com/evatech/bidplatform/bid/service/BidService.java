package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidField;

import java.util.List;
import java.util.Map;

public interface BidService {

    Bid createBid(Long contractId, String title, String createdBy, String userName);
    Bid getBid(Long bidId, String userName);
    Bid updateBidFields(Long bidId, Map<String, String> fields, String userName);
    Bid submitBid(Long bidId, String userName);
    List<BidField> fillBidFormUsingAi(Long bidId);
    List<BidField> getBidFields(Long bidId, String userName);

}
