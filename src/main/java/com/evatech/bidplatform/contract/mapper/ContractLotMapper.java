package com.evatech.bidplatform.contract.mapper;

import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.dto.response.lot.ContractLotResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContractLotMapper {

    @Mapping(target = "lotId", source = "id")

    @Mapping(target = "qualificationStatus",
            expression = "java(toString(contractLot.getQualificationStatus()))")

    @Mapping(target = "analysisStatus",
            expression = "java(toString(contractLot.getAnalysisStatus()))")

    @Mapping(target = "analysisReviewStatus",
            expression = "java(toString(contractLot.getAnalysisReviewStatus()))")

    @Mapping(target = "highlightCount",
            expression = "java(countHighlights(contractLot))")
    ContractLotResponse toResponse(
            ContractLot contractLot
    );

    List<ContractLotResponse> toResponseList(
            List<ContractLot> contractLots
    );

    default String toString(Enum<?> value) {

        if (value == null) {
            return null;
        }

        return value.name();
    }

    default Integer countHighlights(
            ContractLot contractLot
    ) {

        if (contractLot.getHighlights() == null) {
            return 0;
        }

        return contractLot.getHighlights().size();
    }
}