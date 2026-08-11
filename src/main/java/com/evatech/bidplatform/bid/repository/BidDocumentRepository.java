package com.evatech.bidplatform.bid.repository;

import com.evatech.bidplatform.bid.entity.BidDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidDocumentRepository extends JpaRepository<BidDocument, Long> {

    List<BidDocument> findByBidId(Long bidId);
}