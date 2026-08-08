package com.evatech.bidplatform.bid.mapper;

import com.evatech.bidplatform.bid.dto.response.BidFieldResponse;
import com.evatech.bidplatform.bid.entity.BidField;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BidFieldMapper {

    BidFieldResponse toResponse(
            BidField bidField
    );

    List<BidFieldResponse> toResponses(
            List<BidField> bidFields
    );
}