package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.bid.entity.GeneratedDocument;
import com.evatech.bidplatform.user.entity.User;

public interface DocumentGenerator {

    GeneratedDocument generateDocx(
            ContractDocument contract,
            AiBidTemplateResponse documentContent,
            User user);
}