package com.evatech.bidplatform.bid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTemplateSection {

    private String sectionCode;

    private String title;

    private String content;

    private Integer orderNumber;

    private Long contractLotId;

    private String source;

    private Double confidenceScore;
}