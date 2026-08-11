package com.evatech.bidplatform.bid.dto.response;

import com.evatech.bidplatform.bid.entity.BidTemplateFieldType;
import lombok.Builder;

@Builder
public record BidTemplateFieldUpdateResponse(
        Long templateFieldId,
        String placeholder,
        String fieldLabel,
        BidTemplateFieldType fieldType,
        String oldValue,
        String newValue,
        Long contractHighlightId
) {
}