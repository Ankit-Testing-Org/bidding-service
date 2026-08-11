package com.evatech.bidplatform.bid.mapper;

import com.evatech.bidplatform.bid.dto.response.BidDocumentResponse;
import com.evatech.bidplatform.bid.entity.BidDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BidDocumentMapper {
    BidDocumentResponse toResponse(BidDocument document);
}