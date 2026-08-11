package com.evatech.bidplatform.bid.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiGeneratedTemplate {

    private String documentTitle;

    private String executiveSummary;

    private List<AiTemplateSection> sections;
}