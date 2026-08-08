package com.evatech.bidplatform.bid.dto.response;

import com.evatech.bidplatform.bid.entity.BidStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {

    private Long id;

    private String bidReferenceNumber;

    private String title;

    private String createdBy;

    private String currentOwner;

    private LocalDateTime createdAt;

    private LocalDateTime submittedAt;

    private String submittedBy;

    private BidStatus status;

    private Long contractDocumentId;

    private Long contractLotId;

    private String lotNumber;

    private String lotName;
}
