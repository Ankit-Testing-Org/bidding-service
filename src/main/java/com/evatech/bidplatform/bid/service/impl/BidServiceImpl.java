package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidField;
import com.evatech.bidplatform.bid.entity.BidStatus;
import com.evatech.bidplatform.bid.repository.BidFieldRepository;
import com.evatech.bidplatform.bid.repository.BidRepository;
import com.evatech.bidplatform.bid.service.BidService;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.entity.LotQualificationStatus;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.entity.User;
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
    private final ContractService contractService;

    @Override
    public Bid createBid(
            Long contractId,
            String lotNumber,
            String title,
            String createdBy,
            User user, List<String> roles) {

        contractService.getContract(contractId, user, roles);

        List<ContractLot> contractLots = contractService.getContractLots(contractId, user, lotNumber, roles);

        if (contractLots.isEmpty()) {
            throw new IllegalArgumentException("Lot not found: " + lotNumber);
        }

        if (contractLots.size() > 1) {
            throw new IllegalStateException("Multiple lots found for lot number: " + lotNumber);
        }

        ContractLot contractLot = contractLots.get(0);
        if (!LotQualificationStatus.QUALIFIED.equals(
                contractLot.getQualificationStatus())) {
            throw new IllegalStateException("Lot is not selected for bidding");
        }

        List<Bid> existingBid = bidRepository.findByContractLotId(contractLot.getId());
        if (!existingBid.isEmpty()) {
            return existingBid.get(0);
        }
        Bid bid = Bid.builder()
                .contractDocument(
                        contractLot.getContractDocument()
                )
                .contractLot(contractLot)
                .bidReferenceNumber(
                        generateBidReferenceNumber()
                )
                .title(title)
                .createdBy(user.getEmail())
                .currentOwner(user.getEmail())
                .status(BidStatus.DRAFT)
                .build();

        // TODO : NEED TO UPDATE BID OBJECT.

        return bidRepository.save(bid);
    }

    @Override
    @Transactional(readOnly = true)
    public Bid getBid(Long bidId, User user) {
        return bidRepository.findById(bidId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Bid not found with id: " + bidId
                        ));
    }

    @Override
    public Bid updateBidFields(
            Long bidId,
            Map<String, String> fields,
            User user) {

        Bid bid = getBid(bidId, user);
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            BidField bidField = bidFieldRepository.findByBidIdAndFieldName(bidId, fieldName)
                            .orElseGet(() ->
                                    BidField.builder()
                                            .bid(bid)
                                            .fieldName(fieldName)
                                            .manuallyEdited(false)
                                            .build()
                            );

            bidField.setFieldValue(fieldValue);
            bidField.setManuallyEdited(true);
            bidField.setUpdatedAt(LocalDateTime.now());

            bidFieldRepository.save(bidField);
        }
        bid.setStatus(BidStatus.USER_REVIEWED);
        return bidRepository.save(bid);
    }

    @Override
    public Bid submitBid(
            Long bidId,
            User user) {

        Bid bid = getBid(bidId, user);

        if (bid.getStatus() != BidStatus.USER_REVIEWED
                && bid.getStatus() != BidStatus.AI_FORM_FILLED) {
            throw new IllegalStateException(
                    "Only reviewed or AI-filled bid can be submitted"
            );
        }
        bid.setSubmittedBy(user.getEmail());
        bid.setSubmittedAt(LocalDateTime.now());
        bid.setStatus(BidStatus.SUBMITTED_FOR_APPROVAL);
        return bidRepository.save(bid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidField> fillBidFormUsingAi(
            Long bidId,
            User user) {
        return bidFieldRepository.findByBidId(bidId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidField> getBidFields(
            Long bidId,
            User user) {

        Bid bid = getBid(bidId, user);
        return bidFieldRepository.findByBidId(
                bid.getId()
        );
    }

    private String generateBidReferenceNumber() {
        return "BID-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}