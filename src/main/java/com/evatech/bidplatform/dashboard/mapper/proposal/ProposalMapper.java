package com.evatech.bidplatform.dashboard.mapper.proposal;

import com.evatech.bidplatform.dashboard.dto.ProposalResponse;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProposalMapper {

    @Mapping(target = "contractDocumentId",
            source = "contractDocument.id")

    @Mapping(target = "contractDocumentName",
            source = "contractDocument.originalFileName")

    @Mapping(target = "totalLots",
            expression =
                    "java(proposal.getContractDocument() != null ? proposal.getContractDocument().getLots().size() : 0)")

    @Mapping(target = "selectedLots",
            expression =
                    "java(proposal.getContractDocument() != null ? (int) proposal.getContractDocument().getLots().stream().filter(l -> " +
                            "com.evatech.bidplatform.contract.entity.LotQualificationStatus.QUALIFIED.name().equals(l.getQualificationStatus().name())).count() : 0)")
    ProposalResponse toResponse(Proposal proposal);

    List<ProposalResponse> toResponseList(List<Proposal> proposals);
}