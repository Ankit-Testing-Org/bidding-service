package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.analysis.ContractHighlight;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ContractHighlightService {
    List<ContractHighlight> analyseContract(Long contractId, boolean reanalyse, User user, List<String> roles);
}
