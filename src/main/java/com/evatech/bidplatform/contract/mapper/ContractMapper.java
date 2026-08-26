package com.evatech.bidplatform.contract.mapper;

import com.evatech.bidplatform.contract.dto.response.contract.ContractResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    ContractResponse toResponse(
            ContractDocument contractDocument
    );

    List<ContractResponse> toResponses(List<ContractDocument> contractDocuments);
}