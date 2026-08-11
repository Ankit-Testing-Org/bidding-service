package com.evatech.bidplatform.bid.mapper;

import com.evatech.bidplatform.bid.dto.response.GeneratedDocumentResponse;
import com.evatech.bidplatform.document.entity.GeneratedDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GeneratedDocumentMapper {

    @Mapping(
            target = "downloadUrl",
            expression = "java(\"/api/contracts/documents/\" + document.getId() + \"/download\")"
    )
    GeneratedDocumentResponse toResponse(GeneratedDocument document);
}