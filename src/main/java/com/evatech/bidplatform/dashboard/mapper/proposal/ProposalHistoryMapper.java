package com.evatech.bidplatform.dashboard.mapper.proposal;

import com.evatech.bidplatform.dashboard.dto.proposal.response.ProposalHistoryResponse;
import com.evatech.bidplatform.dashboard.entity.ProposalHistory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProposalHistoryMapper {

    ProposalHistoryResponse toResponse(ProposalHistory history);

    List<ProposalHistoryResponse> toResponseList(
            List<ProposalHistory> history
    );
}