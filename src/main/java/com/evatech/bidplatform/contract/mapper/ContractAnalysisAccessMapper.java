package com.evatech.bidplatform.contract.mapper;

import com.evatech.bidplatform.contract.dto.response.contract.ContractAnalysisAccessResponse;
import com.evatech.bidplatform.contract.entity.ContractAssignmentStatus;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractAnalysisAccessMapper {

    @Mapping(target = "contractId", source = "id")
    @Mapping(target = "contractStatus",
            expression = "java(contractDocument.getStatus().name())")
    @Mapping(target = "assignmentStatus",
            expression = "java(contractDocument.getAssignmentStatus().name())")
    @Mapping(target = "assignedTo",
            source = "assignedTo")
    @Mapping(target = "assignedToCurrentUser",
            expression = "java(isAssignedToCurrentUser(contractDocument, currentUser))")
    @Mapping(target = "canOpenAnalysis",
            expression = "java(canOpenAnalysis(contractDocument, currentUser))")
    @Mapping(target = "reason",
            expression = "java(buildReason(contractDocument, currentUser))")
    ContractAnalysisAccessResponse toResponse(
            ContractDocument contractDocument,
            @Context String currentUser
    );

    default boolean isAssignedToCurrentUser(
            ContractDocument contractDocument,
            String currentUser
    ) {

        return ContractAssignmentStatus.ASSIGNED.equals(
                contractDocument.getAssignmentStatus())
                && currentUser.equalsIgnoreCase(
                contractDocument.getAssignedTo());
    }

    default boolean canOpenAnalysis(
            ContractDocument contractDocument,
            String currentUser
    ) {
        return isAssignedToCurrentUser(
                contractDocument,
                currentUser
        );
    }

    default String buildReason(
            ContractDocument contractDocument,
            String currentUser
    ) {

        if (ContractAssignmentStatus.UNASSIGNED.equals(
                contractDocument.getAssignmentStatus())) {
            return "Contract is not assigned";
        }

        if (!isAssignedToCurrentUser(
                contractDocument,
                currentUser)) {
            return "Contract is assigned to another user";
        }

        return null;
    }
}