package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.ContractHighlight;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ContractHighlightService {
    List<ContractHighlight> analyseContract(Long contractId, boolean reanalyse, User user);
}
