package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.bid.dto.AiTemplateFieldDefinition;
import com.evatech.bidplatform.bid.exception.DocumentGenerationException;
import com.evatech.bidplatform.bid.service.DocumentGenerator;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.document.entity.DocumentType;
import com.evatech.bidplatform.document.entity.GeneratedDocument;
import com.evatech.bidplatform.user.entity.User;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
public class DocxDocumentGenerator
        implements DocumentGenerator {

    @Override
    public GeneratedDocument generateDocx(
            ContractDocument contract,
            AiBidTemplateResponse aiBidTemplateResponse,
            User user) {

        try (XWPFDocument document = new XWPFDocument()) {

            XWPFParagraph contentParagraph =
                    document.createParagraph();

            XWPFRun contentRun =
                    contentParagraph.createRun();

            contentRun.setText(
                    aiBidTemplateResponse.documentContent());

            for (AiTemplateFieldDefinition field : aiBidTemplateResponse.fields()) {
                XWPFParagraph fieldParagraph = document.createParagraph();

                XWPFRun fieldLabelRun = fieldParagraph.createRun();

                fieldLabelRun.setBold(true);
                fieldLabelRun.setText(field.fieldLabel());

                XWPFRun placeholderRun = fieldParagraph.createRun();

                placeholderRun.addBreak();
                placeholderRun.setText(
                        "[[" + field.placeholder() + "]]");
                placeholderRun.addBreak();
            }
            String fileName = contract.getOriginalFileName() + "-bid-template.docx";

            String storagePath =
                    saveDocument(
                            document,
                            fileName);

            return GeneratedDocument.builder()
                    .contract(contract)
                    .fileName(fileName)
                    .storagePath(storagePath)
                    .documentType(
                            DocumentType.BID_TEMPLATE)
                    .generatedBy(
                            user.getEmail())
                    .generatedAt(
                            LocalDateTime.now())
                    .build();

        } catch (IOException ex) {

            throw new DocumentGenerationException(
                    "Failed to generate bid template",
                    ex);
        }
    }

    private String saveDocument(
                XWPFDocument document,
                String fileName) throws IOException {

            Path uploadPath =
                    Paths.get("generated-documents");

            Files.createDirectories(uploadPath);

            Path filePath =
                    uploadPath.resolve(fileName);

            try (FileOutputStream out =
                         new FileOutputStream(filePath.toFile())) {

                document.write(out);
            }

            return filePath.toString();
        }
}