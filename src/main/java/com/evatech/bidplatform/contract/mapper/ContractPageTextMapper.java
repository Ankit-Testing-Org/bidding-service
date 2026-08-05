package com.evatech.bidplatform.contract.mapper;

import com.evatech.bidplatform.contract.dto.response.ContractPageTextResponse;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContractPageTextMapper {

    ContractPageTextResponse toResponse(
            ContractPageText contractPageText
    );

    List<ContractPageTextResponse> toResponses(
            List<ContractPageText> contractPageTexts
    );
}
