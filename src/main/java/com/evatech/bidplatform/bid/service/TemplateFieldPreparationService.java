package com.evatech.bidplatform.bid.service;

import com.evatech.bidplatform.bid.dto.AiTemplateFieldDefinition;
import com.evatech.bidplatform.bid.dto.BidTemplateFieldSource;
import com.evatech.bidplatform.bid.dto.BidTemplateFieldType;
import com.evatech.bidplatform.bid.dto.PreparedTemplateField;

import com.evatech.bidplatform.bid.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class TemplateFieldPreparationService {

    private static final int DEFAULT_ENTRY_LINE_COUNT = 2;

    public List<PreparedTemplateField> prepare(
            List<AiTemplateFieldDefinition> aiFields
    ) {
        if (aiFields == null || aiFields.isEmpty()) {
            return List.of();
        }

        List<PreparedTemplateField> preparedFields =
                new ArrayList<>();

        Set<String> usedLogicalNames =
                new HashSet<>();

        int fallbackDisplayOrder = 10;

        for (AiTemplateFieldDefinition aiField : aiFields) {

            validateAiField(aiField);

            String baseLogicalName =
                    normalizeLogicalName(
                            aiField.suggestedLogicalName(),
                            aiField.fieldLabel()
                    );

            String logicalName =
                    makeUniqueLogicalName(
                            baseLogicalName,
                            usedLogicalNames
                    );

            UUID fieldUuid = UUID.randomUUID();

            String placeholder =
                    createPlaceholder(
                            logicalName,
                            fieldUuid
                    );

            PreparedTemplateField preparedField =
                    new PreparedTemplateField(
                            fieldUuid,
                            logicalName,
                            placeholder,
                            aiField.fieldLabel().trim(),
                            resolveDescription(aiField),
                            resolveFieldType(aiField.fieldType()),
                            resolveFieldSource(aiField.source()),
                            aiField.required(),
                            trimToNull(aiField.defaultValue()),
                            resolveAllowedValues(
                                    aiField.allowedValues()
                            ),
                            resolveSectionKey(
                                    aiField.sectionKey()
                            ),
                            resolveSectionTitle(
                                    aiField.sectionTitle()
                            ),
                            aiField.displayOrder() == null
                                    ? fallbackDisplayOrder
                                    : aiField.displayOrder(),
                            resolveEntryLineCount(
                                    aiField.entryLineCount()
                            ),
                            aiField.repeatable(),
                            trimToNull(
                                    aiField.repeatableGroupKey()
                            ),
                            trimToNull(
                                    aiField.calculationExpression()
                            )
                    );

            preparedFields.add(preparedField);

            fallbackDisplayOrder += 10;
        }

        return preparedFields.stream()
                .sorted(
                        Comparator.comparing(
                                PreparedTemplateField::displayOrder
                        )
                )
                .toList();
    }

    private void validateAiField(
            AiTemplateFieldDefinition field
    ) {
        if (field == null) {
            throw new BusinessException(
                    "AI template field cannot be null"
            );
        }

        if (field.fieldLabel() == null
                || field.fieldLabel().isBlank()) {

            throw new BusinessException(
                    "AI template field label is required"
            );
        }
    }

    private String normalizeLogicalName(
            String suggestedLogicalName,
            String fieldLabel
    ) {
        String value = suggestedLogicalName;

        if (value == null || value.isBlank()) {
            value = fieldLabel;
        }

        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "")
                .replaceAll("_+", "_");

        if (normalized.isBlank()) {
            throw new BusinessException(
                    "Unable to generate logical field name"
            );
        }

        if (Character.isDigit(normalized.charAt(0))) {
            normalized = "FIELD_" + normalized;
        }

        return normalized;
    }

    private String makeUniqueLogicalName(
            String baseLogicalName,
            Set<String> usedLogicalNames
    ) {
        String logicalName = baseLogicalName;
        int suffix = 2;

        while (usedLogicalNames.contains(logicalName)) {
            logicalName =
                    baseLogicalName + "_" + suffix;

            suffix++;
        }

        usedLogicalNames.add(logicalName);

        return logicalName;
    }

    private String createPlaceholder(
            String logicalName,
            UUID fieldUuid
    ) {
        return "[[FIELD:"
                + logicalName
                + ":"
                + fieldUuid
                + "]]";
    }

    private String resolveDescription(
            AiTemplateFieldDefinition field
    ) {
        if (field.fieldDescription() != null
                && !field.fieldDescription().isBlank()) {

            return field.fieldDescription().trim();
        }

        return "Enter " + field.fieldLabel().trim() + ".";
    }

    private BidTemplateFieldType resolveFieldType(
            BidTemplateFieldType fieldType
    ) {
        return fieldType == null
                ? BidTemplateFieldType.TEXT
                : fieldType;
    }

    private BidTemplateFieldSource resolveFieldSource(
            BidTemplateFieldSource source
    ) {
        return source == null
                ? BidTemplateFieldSource.USER_INPUT
                : source;
    }

    private List<String> resolveAllowedValues(
            List<String> allowedValues
    ) {
        if (allowedValues == null
                || allowedValues.isEmpty()) {

            return List.of();
        }

        return allowedValues.stream()
                .filter(value -> value != null
                        && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String resolveSectionKey(
            String sectionKey
    ) {
        if (sectionKey == null
                || sectionKey.isBlank()) {

            return "GENERAL";
        }

        return normalizeLogicalName(
                sectionKey,
                sectionKey
        );
    }

    private String resolveSectionTitle(
            String sectionTitle
    ) {
        if (sectionTitle == null
                || sectionTitle.isBlank()) {

            return "General Information";
        }

        return sectionTitle.trim();
    }

    private Integer resolveEntryLineCount(
            Integer entryLineCount
    ) {
        if (entryLineCount == null) {
            return DEFAULT_ENTRY_LINE_COUNT;
        }

        return Math.max(
                1,
                Math.min(entryLineCount, 20)
        );
    }

    private String trimToNull(
            String value
    ) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}