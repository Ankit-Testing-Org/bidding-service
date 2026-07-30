package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidField;
import com.evatech.bidplatform.bid.entity.BidStatus;
import com.evatech.bidplatform.bid.repository.BidFieldRepository;
import com.evatech.bidplatform.bid.repository.BidRepository;
import com.evatech.bidplatform.bid.service.AiBidFormFillPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AiBidFormFillPersistenceServiceImpl implements AiBidFormFillPersistenceService {

    private final BidRepository bidRepository;
    private final BidFieldRepository bidFieldRepository;

    @Override
    public List<BidField> fillBidFormUsingAi(Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new IllegalArgumentException("Bid not found with id: " + bidId));

        List<BidField> generatedFields = createInitialAiGeneratedFields(bid);

        return replaceAiGeneratedFields(bidId, generatedFields);
    }

    @Override
    public List<BidField> replaceAiGeneratedFields(
            Long bidId,
            List<BidField> generatedFields
    ) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new IllegalArgumentException("Bid not found with id: " + bidId));

        bidFieldRepository.deleteByBidId(bidId);

        for (BidField generatedField : generatedFields) {
            generatedField.setBid(bid);
            generatedField.setManuallyEdited(false);
            bidFieldRepository.save(generatedField);
        }

        bid.setStatus(BidStatus.AI_FORM_FILLED);
        bidRepository.save(bid);

        return bidFieldRepository.findByBidId(bidId);
    }

    private List<BidField> createInitialAiGeneratedFields(Bid bid) {
        List<BidField> fields = new ArrayList<>();

        fields.add(createField(
                bid,
                "bidTitle",
                bid.getTitle(),
                null,
                1.0
        ));

        fields.add(createField(
                bid,
                "clientName",
                "Not found",
                null,
                0.0
        ));

        fields.add(createField(
                bid,
                "tenderReference",
                bid.getBidReferenceNumber(),
                null,
                1.0
        ));

        fields.add(createField(
                bid,
                "submissionDeadline",
                "Not found",
                null,
                0.0
        ));

        fields.add(createField(
                bid,
                "contractDuration",
                "Not found",
                null,
                0.0
        ));

        fields.add(createField(
                bid,
                "scopeOfWork",
                "Not found",
                null,
                0.0
        ));

        fields.add(createField(
                bid,
                "eligibilityCriteria",
                "Not found",
                null,
                0.0
        ));

        fields.add(createField(
                bid,
                "pricingRequirement",
                "Not found",
                null,
                0.0
        ));

        fields.add(createField(
                bid,
                "requiredDocuments",
                "Not found",
                null,
                0.0
        ));

        fields.add(createField(
                bid,
                "penaltyClauses",
                "Not found",
                null,
                0.0
        ));

        fields.add(createField(
                bid,
                "riskSummary",
                "Not found",
                null,
                0.0
        ));

        return fields;
    }

    private BidField createField(
            Bid bid,
            String fieldName,
            String fieldValue,
            Integer sourcePageNumber,
            Double confidenceScore
    ) {
        return BidField.builder()
                .bid(bid)
                .fieldName(fieldName)
                .fieldValue(fieldValue)
                .sourcePageNumber(sourcePageNumber)
                .confidenceScore(confidenceScore)
                .manuallyEdited(false)
                .build();
    }
}