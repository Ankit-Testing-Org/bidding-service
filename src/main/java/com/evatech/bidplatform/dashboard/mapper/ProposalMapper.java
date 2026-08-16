package com.evatech.bidplatform.dashboard.mapper;

import com.evatech.bidplatform.dashboard.dto.ProposalResponse;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProposalMapper {

    @Mapping(
            target = "contractDocumentId",
            source = "contractDocument.id"
    )
    @Mapping(
            target = "contractDocumentName",
            source = "contractDocument.originalFileName"
    )
    @Mapping(
            target = "totalLots",
            ignore = true
    )
    @Mapping(
            target = "selectedLots",
            ignore = true
    )
    ProposalResponse toResponse(
            Proposal proposal
    );
}