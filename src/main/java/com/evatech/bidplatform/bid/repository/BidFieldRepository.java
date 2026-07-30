package com.evatech.bidplatform.bid.repository;

import com.evatech.bidplatform.bid.entity.BidField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidFieldRepository extends JpaRepository<BidField, Long> {

    List<BidField> findByBidId(Long bidId);

    Optional<BidField> findByBidIdAndFieldName(Long bidId, String fieldName);

    boolean existsByBidIdAndFieldName(Long bidId, String fieldName);

    void deleteByBidId(Long bidId);
}