package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidField;
import com.evatech.bidplatform.bid.entity.BidStatus;
import com.evatech.bidplatform.bid.repository.BidFieldRepository;
import com.evatech.bidplatform.bid.repository.BidRepository;
import com.evatech.bidplatform.bid.service.BidService;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final BidFieldRepository bidFieldRepository;
    private final ContractDocumentRepository contractDocumentRepository;

    @Override
    public Bid createBid(Long contractId, String title, String createdBy, String userName) {
        ContractDocument contractDocument = contractDocumentRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found with id: " + contractId));

        Bid bid = Bid.builder()
                .contractDocument(contractDocument)
                .bidReferenceNumber(generateBidReferenceNumber())
                .title(title)
                .createdBy(createdBy)
                .currentOwner(userName)
                .createdAt(LocalDateTime.now())
                .status(BidStatus.DRAFT)
                .build();

        return bidRepository.save(bid);
    }

    @Override
    @Transactional(readOnly = true)
    public Bid getBid(Long bidId, String userName) {
        return bidRepository.findById(bidId)
                .orElseThrow(() -> new IllegalArgumentException("Bid not found with id: " + bidId));
    }

    @Override
    public Bid updateBidFields(Long bidId, Map<String, String> fields, String userName) {
        Bid bid = getBid(bidId, userName);

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            BidField bidField = bidFieldRepository.findByBidIdAndFieldName(bidId, fieldName)
                    .orElseGet(() -> BidField.builder()
                            .bid(bid)
                            .fieldName(fieldName)
                            .manuallyEdited(false)
                            .build());

            bidField.setFieldValue(fieldValue);
            bidField.setManuallyEdited(true);
            bidField.setUpdatedAt(LocalDateTime.now());
            bidFieldRepository.save(bidField);
        }

        bid.setStatus(BidStatus.USER_REVIEWED);
        return bidRepository.save(bid);
    }

    @Override
    public Bid submitBid(Long bidId, String userName) {
        Bid bid = getBid(bidId, userName);

        if (bid.getStatus() != BidStatus.USER_REVIEWED
                && bid.getStatus() != BidStatus.AI_FORM_FILLED) {
            throw new IllegalStateException("Only reviewed or AI-filled bid can be submitted");
        }
        bid.setSubmittedBy(userName);
        bid.setStatus(BidStatus.SUBMITTED_FOR_APPROVAL);
        bid.setSubmittedAt(LocalDateTime.now());

        return bidRepository.save(bid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidField> fillBidFormUsingAi(Long bidId) {
        return bidFieldRepository.findByBidId(bidId);
    }

    private String generateBidReferenceNumber() {
        return "BID-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidField> getBidFields(Long bidId, String userName) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new IllegalArgumentException("Bid not found with id: " + bidId));

        return bidFieldRepository.findByBidId(bid.getId());
    }
}