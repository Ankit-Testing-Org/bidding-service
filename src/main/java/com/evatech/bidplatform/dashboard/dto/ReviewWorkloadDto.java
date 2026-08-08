package com.evatech.bidplatform.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewWorkloadDto {

    private Long legalReviews;

    private Long financeReviews;

    private Long commercialReviews;

    private Long managerApprovals;
}