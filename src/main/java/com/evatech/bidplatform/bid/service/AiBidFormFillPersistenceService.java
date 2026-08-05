package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.bid.entity.BidField;

import java.util.List;

public interface AiBidFormFillPersistenceService {

    List<BidField> replaceAiGeneratedFields(Long bidId, List<BidField> generatedFields);
    List<BidField> fillBidFormUsingAi(Long bidId, String userName);

}
