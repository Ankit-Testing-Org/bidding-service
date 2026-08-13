package com.evatech.bidplatform.bid.repository;

import com.evatech.bidplatform.bid.entity.GeneratedDocument;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {
    Optional<GeneratedDocument> findByIdAndContract(Long documentId, ContractDocument contract);
}
