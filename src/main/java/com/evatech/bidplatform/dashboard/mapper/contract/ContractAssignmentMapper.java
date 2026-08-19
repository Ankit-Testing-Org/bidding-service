package com.evatech.bidplatform.dashboard.mapper.contract;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.dashboard.dto.contract.response.AssignContractResponse;
import com.evatech.bidplatform.dashboard.dto.contract.response.UnassignContractResponse;
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