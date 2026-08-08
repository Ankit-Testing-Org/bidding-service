package com.evatech.bidplatform.bid.mapper;

import com.evatech.bidplatform.bid.dto.response.BidResponse;
import com.evatech.bidplatform.bid.entity.Bid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BidMapper {

    @Mapping(target = "contractDocumentId",
            source = "contractDocument.id")
    @Mapping(target = "contractLotId",
            source = "contractLot.id")
    @Mapping(target = "lotNumber",
            source = "contractLot.lotNumber")
    @Mapping(target = "lotName",
            source = "contractLot.lotName")
    BidResponse toResponse(Bid bid);

    List<BidResponse> toResponses(List<Bid> bids);
}
