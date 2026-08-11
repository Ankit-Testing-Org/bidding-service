package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.bid.dto.AiTemplateFieldDefinition;
import com.evatech.bidplatform.bid.entity.BidTemplateField;
import com.evatech.bidplatform.bid.exception.DocumentGenerationException;
import com.evatech.bidplatform.bid.exception.DocumentNotFoundException;
import com.evatech.bidplatform.bid.repository.BidTemplateFieldRepository;
import com.evatech.bidplatform.bid.service.AiTemplateGenerationService;
import com.evatech.bidplatform.bid.service.BidTemplateService;
import com.evatech.bidplatform.bid.service.DocumentGenerator;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import com.evatech.bidplatform.document.entity.GeneratedDocument;
import com.evatech.bidplatform.document.repository.GeneratedDocumentRepository;
import com.evatech.bidplatform.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BidTemplateServiceImpl
        implements BidTemplateService {

    private final ContractDocumentRepository contractRepository;
    private final AiTemplateGenerationService aiTemplateGenerationService;
    private final DocumentGenerator documentGenerator;
    private final GeneratedDocumentRepository generatedDocumentRepository;
    private final BidTemplateFieldRepository bidTemplateFieldRepository;
    @Override
    @Transactional
    public GeneratedDocument generateBidTemplate(
            Long contractId,
            User user,
            List<String> roles) {

        ContractDocument contract =
                contractRepository.findById(contractId)
                        .orElseThrow();

        List<ContractLot> qualifiedLots =
                contract.getLots()
                        .stream()
                        .filter(ContractLot::isQualified)
                        .toList();

        AiBidTemplateResponse response = aiTemplateGenerationService.generateBidTemplate(contract, qualifiedLots);

        GeneratedDocument generatedDocument = documentGenerator.generateDocx(contract, response, user);

        generatedDocument = generatedDocumentRepository.save(generatedDocument);

        createTemplateFields(generatedDocument, response);

        return generatedDocument;
    }

    @Override
    @Transactional
    public Resource downloadDocument(Long documentId) {

        GeneratedDocument document = generatedDocumentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        try {
            Path path = Paths.get(document.getStoragePath());
            return new UrlResource(path.toUri());
        } catch (MalformedURLException ex) {

            throw new DocumentGenerationException(
                    "Unable to read generated document",
                    ex);
        }
    }

    private void createTemplateFields(
            GeneratedDocument generatedDocument,
            AiBidTemplateResponse response) {

        List<BidTemplateField> fields =
                new ArrayList<>();

        int sequence = 1;

        for (AiTemplateFieldDefinition field : response.fields()) {
            fields.add(
                    BidTemplateField.builder()
                            .generatedDocument(
                                    generatedDocument)
                            .placeholder(
                                    "FIELD_" + sequence)
                            .fieldLabel(
                                    field.fieldLabel())
                            .fieldType(
                                    field.fieldType())
                            .required(
                                    field.required())
                            .defaultValue(
                                    field.defaultValue())
                            .build());

            sequence++;
        }
        bidTemplateFieldRepository.saveAll(fields);
    }

}
