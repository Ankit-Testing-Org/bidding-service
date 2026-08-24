package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.bid.dto.PreparedTemplateField;
import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.GeneratedDocument;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface BidDocumentGenerator {

    GeneratedDocument generateDocx(
            Bid bid,
            ContractDocument contract,
            AiBidTemplateResponse aiResponse,
            List<PreparedTemplateField> fields,
            User user
    );
}