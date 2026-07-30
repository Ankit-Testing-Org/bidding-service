package com.evatech.bidplatform.bid.repository;

import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findByBidReferenceNumber(String bidReferenceNumber);

    List<Bid> findByContractDocumentId(Long contractDocumentId);

    List<Bid> findByCreatedBy(String createdBy);

    List<Bid> findByStatus(BidStatus status);

    boolean existsByBidReferenceNumber(String bidReferenceNumber);
}