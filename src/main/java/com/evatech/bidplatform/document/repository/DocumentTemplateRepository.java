package com.evatech.bidplatform.document.repository;

import com.evatech.bidplatform.document.entity.DocumentTemplate;
import com.evatech.bidplatform.document.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    Optional<DocumentTemplate> findByTemplateCode(String templateCode);

    List<DocumentTemplate> findByDocumentType(DocumentType documentType);

    List<DocumentTemplate> findByDocumentTypeAndActiveTrue(DocumentType documentType);

    Optional<DocumentTemplate> findByTemplateCodeAndActiveTrue(String templateCode);

    boolean existsByTemplateCode(String templateCode);
}