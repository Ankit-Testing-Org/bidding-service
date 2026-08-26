package com.evatech.bidplatform.contract.dto.request.contract;

public record ContractSearchRequest(
        String searchText,
        String status,
        String assignmentStatus,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
}