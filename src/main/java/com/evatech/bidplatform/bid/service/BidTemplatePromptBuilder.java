package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BidTemplatePromptBuilder {

    public String build(
            ContractDocument contract,
            List<ContractLot> qualifiedLots) {

        StringBuilder prompt = new StringBuilder();
        return prompt.toString();
    }
}