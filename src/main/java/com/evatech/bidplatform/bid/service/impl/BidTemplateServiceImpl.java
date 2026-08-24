package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.bid.dto.AiBidTemplateResponse;
import com.evatech.bidplatform.bid.dto.PreparedTemplateField;
import com.evatech.bidplatform.bid.entity.*;
import com.evatech.bidplatform.bid.exception.BusinessException;
import com.evatech.bidplatform.bid.repository.BidRepository;
import com.evatech.bidplatform.bid.repository.BidTemplateFieldRepository;
import com.evatech.bidplatform.bid.repository.GeneratedDocumentRepository;
import com.evatech.bidplatform.bid.service.AiTemplateGenerationService;
import com.evatech.bidplatform.bid.service.BidDocumentGenerator;
import com.evatech.bidplatform.bid.service.BidTemplateService;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidDocumentType;
import com.evatech.bidplatform.bid.entity.BidReviewStatus;
import com.evatech.bidplatform.bid.entity.BidStatus;
import com.evatech.bidplatform.bid.entity.BidTemplateField;
import com.evatech.bidplatform.bid.entity.GeneratedDocument;
import com.evatech.bidplatform.bid.service.TemplateFieldPreparationService;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BidTemplateServiceImpl implements BidTemplateService {

    private final BidRepository bidRepository;

    private final GeneratedDocumentRepository generatedDocumentRepository;

    private final BidTemplateFieldRepository fieldRepository;

    private final ContractService contractService;

    private final AiTemplateGenerationService aiTemplateGenerationService;

    private final TemplateFieldPreparationService fieldPreparationService;

    private final BidDocumentGenerator documentGenerator;

    @Override
    public GeneratedDocument generateBidTemplate(Long contractId, User user, List<String> roles) {
        log.info("Generating bid template for contract {}", contractId);

        ContractDocument contract = contractService.getContract(contractId, user, roles);

        List<ContractLot> qualifiedLots = contract.getLots().stream().filter(ContractLot::isQualified).toList();

        if (qualifiedLots.isEmpty()) {
            throw new BusinessException("Bid template cannot be generated because " + "no qualified lots were found");
        }

        Bid bid = createOrGetDraftBid(contract, user);

        AiBidTemplateResponse aiResponse = aiTemplateGenerationService.generateBidTemplate(contract, qualifiedLots);

        validateAiResponse(aiResponse);

        List<PreparedTemplateField> fields = fieldPreparationService.prepare(aiResponse.fields());

        if (fields.isEmpty()) {
            throw new BusinessException("AI did not return any template fields");
        }

        GeneratedDocument generatedDocument = documentGenerator.generateDocx(bid,
                contract, aiResponse, fields, user);

        generatedDocument.setBid(bid);

        generatedDocument.setDocumentType(BidDocumentType.GENERATED_TEMPLATE);

        generatedDocument = generatedDocumentRepository.save(generatedDocument);

        createTemplateFields(generatedDocument, fields);

        bid.setStatus(BidStatus.TEMPLATE_GENERATED);

        bidRepository.save(bid);

        log.info("Generated bid template document {} " + "for bid {} and contract {}", generatedDocument.getId(), bid.getId(), contractId);

        return generatedDocument;
    }

    private Bid createOrGetDraftBid(ContractDocument contract, User user) {
        Optional<Bid> existingBid = bidRepository.findFirstByContractDocumentIdOrderByCreatedAtDesc(contract.getId());

        if (existingBid.isPresent()) {
            return existingBid.get();
        }

        Bid bid = Bid.builder().bidReferenceNumber(generateReference()).title(buildBidTitle(contract)).contractDocument(contract).createdBy(user.getEmail()).currentOwner(user.getEmail()).status(BidStatus.DRAFT).reviewStatus(BidReviewStatus.PENDING).build();

        return bidRepository.save(bid);
    }

    private String buildBidTitle(ContractDocument contract) {
        String originalFileName = contract.getOriginalFileName();

        if (originalFileName == null || originalFileName.isBlank()) {

            return "Bid for Contract " + contract.getId();
        }

        int extensionIndex = originalFileName.lastIndexOf('.');

        if (extensionIndex > 0) {
            return originalFileName.substring(0, extensionIndex);
        }

        return originalFileName;
    }

    private String generateReference() {
        return "BID-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateAiResponse(AiBidTemplateResponse aiResponse) {
        if (aiResponse == null) {
            throw new BusinessException("AI template generation returned no response");
        }

        if (aiResponse.fields() == null || aiResponse.fields().isEmpty()) {

            throw new BusinessException("AI template generation returned no fields");
        }
    }

    private void createTemplateFields(GeneratedDocument generatedDocument, List<PreparedTemplateField> preparedFields) {
        List<BidTemplateField> entities = preparedFields.stream().map(field -> mapTemplateField(generatedDocument, field)).toList();

        fieldRepository.saveAll(entities);

        generatedDocument.getTemplateFields().addAll(entities);
    }

    private BidTemplateField mapTemplateField(GeneratedDocument generatedDocument, PreparedTemplateField field) {
        return BidTemplateField.builder().generatedDocument(generatedDocument).fieldUuid(field.fieldUuid()).logicalName(field.logicalName()).placeholder(field.placeholder()).fieldLabel(field.fieldLabel()).fieldDescription(field.fieldDescription()).fieldType(field.fieldType()).source(field.source()).required(field.required()).defaultValue(field.defaultValue()).allowedValues(field.allowedValues()).sectionKey(field.sectionKey()).sectionTitle(field.sectionTitle()).displayOrder(field.displayOrder()).entryLineCount(field.entryLineCount()).repeatable(field.repeatable()).repeatableGroupKey(field.repeatableGroupKey()).calculationExpression(field.calculationExpression()).fieldValue(null).completed(false).build();
    }
}