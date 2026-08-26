package com.evatech.bidplatform.contract.mapper;

import com.evatech.bidplatform.contract.dto.response.contract.AssignContractResponse;
import com.evatech.bidplatform.contract.dto.response.contract.UnassignContractResponse;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractAssignmentMapper {

    @Mapping(target = "contractId", source = "id")
    @Mapping(target = "contractName", source = "originalFileName")
    @Mapping(target = "assignmentStatus",
            expression = "java(contractDocument.getAssignmentStatus().name())")
    @Mapping(target = "message",
            constant = "Contract assigned successfully")
    AssignContractResponse toAssignResponse(
            ContractDocument contractDocument
    );

    @Mapping(target = "success", constant = "true")
    @Mapping(target = "contractId", source = "id")
    @Mapping(target = "message", constant = "Contract unassigned successfully")
    UnassignContractResponse toUnassignResponse(
            ContractDocument contractDocument
    );
}