package com.evatech.bidplatform.contract.mapper;

import com.evatech.bidplatform.contract.dto.response.ContractLotResponse;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContractLotMapper {

    ContractLotResponse toResponse(
            ContractLot contractLot
    );

    List<ContractLotResponse> toResponses(List<ContractLot> contractLots);
}