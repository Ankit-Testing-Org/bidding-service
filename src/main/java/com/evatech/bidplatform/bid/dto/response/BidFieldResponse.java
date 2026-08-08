package com.evatech.bidplatform.bid.dto.response;

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
public class BidFieldResponse {

    private Long id;

    private String fieldName;

    private String fieldValue;

    private Integer sourcePageNumber;

    private Double confidenceScore;

    private Boolean manuallyEdited;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}