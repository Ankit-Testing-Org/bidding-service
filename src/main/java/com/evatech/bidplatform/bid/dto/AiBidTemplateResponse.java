package com.evatech.bidplatform.bid.dto;

import java.util.List;

public record AiBidTemplateResponse(

        String documentTitle,

        String introductoryContent,

        List<AiTemplateFieldDefinition> fields
) {
}