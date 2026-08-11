package com.evatech.bidplatform.bid.dto;

import java.util.List;

public record AiBidTemplateResponse(
        String documentContent,
        List<AiTemplateFieldDefinition> fields
) {
}