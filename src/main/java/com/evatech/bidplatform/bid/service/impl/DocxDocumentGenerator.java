package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.bid.dto.PreparedTemplateField;
import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidDocumentType;
import com.evatech.bidplatform.bid.exception.DocumentGenerationException;
import com.evatech.bidplatform.bid.service.BidDocumentGenerator;
import com.evatech.bidplatform.bid.service.FileStorageService;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.bid.entity.GeneratedDocument;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocxDocumentGenerator implements BidDocumentGenerator {

    private final FileStorageService fileStorageService;

    @Override
    public GeneratedDocument generateDocx(Bid bid, ContractDocument contract, AiBidTemplateResponse aiBidTemplateResponse, List<PreparedTemplateField> fields, User user) {

        try (XWPFDocument document = new XWPFDocument()) {

            XWPFParagraph contentParagraph = document.createParagraph();

            XWPFRun contentRun = contentParagraph.createRun();

            contentRun.setText(aiBidTemplateResponse.introductoryContent());

            for (PreparedTemplateField field : fields) {
                XWPFParagraph fieldParagraph = document.createParagraph();
                XWPFRun fieldLabelRun = fieldParagraph.createRun();
                fieldLabelRun.setBold(true);
                fieldLabelRun.setText(field.fieldLabel());
                XWPFRun instructionRun = fieldParagraph.createRun();
                instructionRun.addBreak();
                instructionRun.setItalic(true);
                instructionRun.setText(field.fieldDescription());
                XWPFRun metadataRun = fieldParagraph.createRun();
                metadataRun.addBreak();
                metadataRun.setText("Field: " + field.logicalName());
                XWPFRun placeholderRun = fieldParagraph.createRun();
                placeholderRun.addBreak();
                placeholderRun.setText(field.placeholder());
                placeholderRun.addBreak();
                for (int i = 0; i < field.entryLineCount(); i++) {
                    XWPFRun emptyLine = fieldParagraph.createRun();
                    emptyLine.addBreak();
                    emptyLine.setText("____________________________________________");
                }
            }
            String fileName = contract.getOriginalFileName() + "-bid-template.docx";
            String storagePath = fileStorageService.storeTemplate(fileName, document);
            return GeneratedDocument.builder().
                    fileName(fileName).
                    storagePath(storagePath).
                    documentType(BidDocumentType.GENERATED_TEMPLATE).
                    generatedBy(user.getEmail()).
                    generatedAt(LocalDateTime.now()).
                    bid(bid).
                    build();
        } catch (IOException ex) {

            throw new DocumentGenerationException("Failed to generate bid template", ex);
        }
    }
}