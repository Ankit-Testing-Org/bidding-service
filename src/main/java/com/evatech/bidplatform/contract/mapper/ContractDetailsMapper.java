package com.evatech.bidplatform.contract.mapper;

import com.evatech.bidplatform.contract.dto.response.contract.ContractDetailsResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.LotQualificationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractDetailsMapper {

    @Mapping(target = "contractId", source = "id")
    @Mapping(target = "contractName", source = "originalFileName")

    @Mapping(target = "fileName", source = "originalFileName")
    @Mapping(target = "fileType", source = "fileType")

    @Mapping(target = "status",
            expression = "java(contractDocument.getStatus().name())")

    @Mapping(target = "assignmentStatus",
            expression = "java(resolveAssignmentStatus(contractDocument))")

    @Mapping(target = "assignedToName",
            source = "assignedTo")

    @Mapping(target = "lotCount",
            expression = "java(countLots(contractDocument))")

    @Mapping(target = "qualifiedLotCount",
            expression = "java(countQualifiedLots(contractDocument))")

    @Mapping(target = "highlightCount",
            expression = "java(countHighlights(contractDocument))")

    @Mapping(target = "bidCreated",
            expression = "java(hasBids(contractDocument))")

    @Mapping(target = "pdfUrl",
            expression = "java(buildPdfUrl(contractDocument))")
    ContractDetailsResponse toResponse(
            ContractDocument contractDocument
    );

    default String resolveAssignmentStatus(
            ContractDocument contractDocument
    ) {

        if (contractDocument.getAssignmentStatus() == null) {
            return "UNASSIGNED";
        }

        return contractDocument.getAssignmentStatus().name();
    }

    default Integer countLots(
            ContractDocument contractDocument
    ) {
        return contractDocument.getLots() == null
                ? 0
                : contractDocument.getLots().size();
    }

    default Integer countHighlights(
            ContractDocument contractDocument
    ) {
        return contractDocument.getHighlights() == null
                ? 0
                : contractDocument.getHighlights().size();
    }

    default Integer countQualifiedLots(
            ContractDocument contractDocument
    ) {

        if (contractDocument.getLots() == null) {
            return 0;
        }

        return Math.toIntExact(
                contractDocument.getLots()
                        .stream()
                        .filter(lot ->
                                lot.getQualificationStatus()
                                        == LotQualificationStatus.QUALIFIED)
                        .count()
        );
    }

    default boolean hasBids(
            ContractDocument contractDocument
    ) {
        return contractDocument.getBids() != null
                && !contractDocument.getBids().isEmpty();
    }

    default String buildPdfUrl(
            ContractDocument contractDocument
    ) {
        return "/api/contracts/"
                + contractDocument.getId()
                + "/pdf";
    }
}