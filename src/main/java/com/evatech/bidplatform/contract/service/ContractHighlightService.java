package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.user.dto.response.ContractAnalysisPageResponse;
import com.evatech.bidplatform.user.dto.response.ContractHighlightResponse;
import com.evatech.bidplatform.user.entity.User;

import java.util.List;

public interface ContractHighlightService {

    ContractAnalysisPageResponse analyseContractHighlights(Long contractId, boolean reanalyse, User user, List<String> roles);
    ContractHighlightResponse approveHighlight(Long highlightId, User user);
    ContractHighlightResponse rejectHighlight(Long highlightId, String comment, User user);
    List<ContractHighlightResponse> getHighlights(Long contractId);
    ContractHighlightResponse reanalyseHighlight(Long highlightId, User user, String userComment);
}
