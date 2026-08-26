package com.evatech.bidplatform.contract.dto.response.contract;

import java.util.List;

public record ContractSearchResponse(
        List<ContractListItemResponse> content,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        boolean first,
        boolean last
) {
}