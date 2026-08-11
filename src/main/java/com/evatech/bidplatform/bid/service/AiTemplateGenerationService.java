package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;

import java.util.List;

public interface AiTemplateGenerationService {

    AiBidTemplateResponse generateBidTemplate(
            ContractDocument contract,
            List<ContractLot> qualifiedLots);
}