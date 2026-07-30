package com.evatech.bidplatform.document.repository;

import com.evatech.bidplatform.document.entity.DocumentType;
import com.evatech.bidplatform.document.entity.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {

    List<GeneratedDocument> findByBidId(Long bidId);

    List<GeneratedDocument> findByBidIdAndDocumentType(
            Long bidId,
            DocumentType documentType
    );

    List<GeneratedDocument> findByGeneratedBy(String generatedBy);

    boolean existsByStoragePath(String storagePath);
}
