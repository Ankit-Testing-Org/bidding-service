package com.evatech.bidplatform.bid.repository;

import com.evatech.bidplatform.bid.entity.BidTemplateField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidTemplateFieldRepository
        extends JpaRepository<BidTemplateField, Long> {

    List<BidTemplateField> findByGeneratedDocumentId(Long generatedDocumentId);

    Optional<BidTemplateField> findByPlaceholder(String placeholder);
}
