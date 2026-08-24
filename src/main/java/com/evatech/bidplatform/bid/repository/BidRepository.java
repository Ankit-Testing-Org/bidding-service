package com.evatech.bidplatform.bid.repository;

import com.evatech.bidplatform.bid.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository
        extends JpaRepository<Bid, Long> {

    List<Bid> findByContractDocumentId(Long contractId);

    Optional<Bid> findFirstByContractDocumentIdOrderByCreatedAtDesc(Long contractId);
}
