package com.evatech.bidplatform.bid.repository;

import com.evatech.bidplatform.bid.entity.BidDocument;
import com.evatech.bidplatform.bid.entity.BidDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidDocumentRepository extends JpaRepository<BidDocument, Long> {

    List<BidDocument> findByBidId(Long bidId);

    Optional<BidDocument> findByIdAndBidId(Long documentId, Long bidId);

    List<BidDocument> findByBidIdAndDocumentType(Long bidId, BidDocumentType documentType);
}