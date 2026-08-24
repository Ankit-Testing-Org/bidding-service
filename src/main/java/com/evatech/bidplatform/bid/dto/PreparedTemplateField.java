package com.evatech.bidplatform.bid.dto;

import java.util.List;
import java.util.UUID;

public record PreparedTemplateField(

        UUID fieldUuid,

        String logicalName,

        String placeholder,

        String fieldLabel,

        String fieldDescription,

        BidTemplateFieldType fieldType,

        BidTemplateFieldSource source,

        boolean required,

        String defaultValue,

        List<String> allowedValues,

        String sectionKey,

        String sectionTitle,

        Integer displayOrder,

        Integer entryLineCount,

        boolean repeatable,

        String repeatableGroupKey,

        String calculationExpression
) {
}