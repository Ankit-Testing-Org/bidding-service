package com.evatech.bidplatform.bid.repository;

import com.evatech.bidplatform.bid.entity.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {

    Optional<GeneratedDocument> findByIdAndBidContractDocumentId(Long documentId, Long contractId);

    Optional<GeneratedDocument> findFirstByBidContractDocumentIdOrderByGeneratedAtDesc(Long contractId);
}