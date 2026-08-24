package com.evatech.bidplatform.bid.mapper;

import com.evatech.bidplatform.bid.dto.response.BidDocumentDetailsResponse;
import com.evatech.bidplatform.bid.entity.GeneratedDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BidDocumentMapper {

    default BidDocumentDetailsResponse toDocumentResponse(GeneratedDocument document) {
        Long contractId = document.getBid().getContractDocument().getId();
        return new BidDocumentDetailsResponse(
                document.getId(),
                document.getBid().getId(),
                contractId,
                document.getFileName(),
                document.getBid().getContractDocument().getOriginalFileName(),
                document.getContentType(),
                document.getFileSize(),
                document.getDocumentType(),
                "GENERATED",
                "/api/contracts/" + contractId + "/documents/" + document.getId() + "/download",
                "/api/contracts/" + contractId + "/documents/" + document.getId() + "/preview",
                document.getGeneratedAt(),
                document.getGeneratedBy());
    }
}