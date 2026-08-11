package com.evatech.bidplatform.bid.dto;

import com.evatech.bidplatform.bid.entity.BidTemplateFieldType;

public record AiTemplateFieldDefinition(
        String placeholder,
        String fieldLabel,
        BidTemplateFieldType fieldType,
        boolean required,
        String defaultValue
) {
}
