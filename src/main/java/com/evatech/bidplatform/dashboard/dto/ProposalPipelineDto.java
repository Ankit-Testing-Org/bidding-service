package com.evatech.bidplatform.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProposalPipelineDto {

    private long draft;

    private long submitted;

    private long review;

    private long awarded;

    private long lost;

    private long notBidded;
}
