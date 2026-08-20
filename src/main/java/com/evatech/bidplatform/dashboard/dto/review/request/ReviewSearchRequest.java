package com.evatech.bidplatform.dashboard.dto.review.request;

import com.evatech.bidplatform.dashboard.dto.review.ReviewStatus;
import com.evatech.bidplatform.dashboard.dto.review.ReviewType;

public record ReviewSearchRequest(

        String searchText,
        ReviewStatus status,
        ReviewType reviewType,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection

) {
}