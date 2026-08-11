package com.evatech.bidplatform.bid.service;


import com.evatech.bidplatform.bid.dto.ParsedTemplateFieldValue;
import com.evatech.bidplatform.bid.exception.DocumentGenerationException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UploadedTemplateParser {

    private static final Pattern FIELD_PATTERN =
            Pattern.compile("\\[\\[(FIELD_\\d+)\\]\\]\\s*(.*)");

    public List<ParsedTemplateFieldValue> parse(Resource resource) {

        List<ParsedTemplateFieldValue> values =
                new ArrayList<>();

        try (InputStream inputStream = resource.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {

            for (XWPFParagraph paragraph : document.getParagraphs()) {

                String text = paragraph.getText();

                if (text == null || text.isBlank()) {
                    continue;
                }

                Matcher matcher =
                        FIELD_PATTERN.matcher(text.trim());

                if (matcher.find()) {

                    String placeholder =
                            matcher.group(1);

                    String value =
                            matcher.group(2);

                    values.add(
                            new ParsedTemplateFieldValue(
                                    placeholder,
                                    value));
                }
            }

            return values;

        } catch (IOException ex) {

            throw new DocumentGenerationException(
                    "Failed to parse uploaded template",
                    ex);
        }
    }
}