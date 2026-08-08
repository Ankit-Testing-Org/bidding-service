package com.evatech.bidplatform.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OverviewSummaryDto {

    private long activeProposalCount;
    private BigDecimal activeProposalValue;

    private long closedProposalCount;
    private BigDecimal closedProposalValue;

    private long wonProposalCount;
    private BigDecimal wonProposalValue;

    private long lostProposalCount;
    private BigDecimal lostProposalValue;

    private long notBiddedProposalCount;
    private BigDecimal notBiddedProposalValue;

    private long noBidProposalCount;
    private BigDecimal noBidProposalValue;
}