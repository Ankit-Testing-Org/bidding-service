package com.evatech.bidplatform.dashboard.dto.contract.request;

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