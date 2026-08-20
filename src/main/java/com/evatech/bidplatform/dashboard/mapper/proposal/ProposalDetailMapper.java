package com.evatech.bidplatform.dashboard.mapper.proposal;

import com.evatech.bidplatform.dashboard.dto.proposal.response.ProposalDetailResponse;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import com.evatech.bidplatform.dashboard.entity.ProposalHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {
                ProposalMapper.class,
                ProposalHistoryMapper.class
        }
)
public interface ProposalDetailMapper {

    @Mapping(target = "proposal", source = "proposal")
    @Mapping(target = "history", source = "history")
    ProposalDetailResponse toResponse(
            Proposal proposal,
            List<ProposalHistory> history
    );
}