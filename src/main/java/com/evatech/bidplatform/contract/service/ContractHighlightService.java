package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.analysis.ContractHighlight;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ContractHighlightService {

    List<ContractHighlight> analyseContractHightlights(Long contractId, boolean reanalyse, User user, List<String> roles);
    ContractHighlight approveHighlight(Long highlightId, User user);
    ContractHighlight rejectHighlight(Long highlightId, String comment, User user);
    ContractHighlight requestReanalysis(Long highlightId, String comment, User user);
}
