package com.evatech.bidplatform.bid.repository;

import com.evatech.bidplatform.bid.entity.BidTemplateField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BidTemplateFieldRepository
        extends JpaRepository<BidTemplateField, Long> {

    List<BidTemplateField>
    findAllByGeneratedDocumentIdOrderByDisplayOrderAsc(
            Long generatedDocumentId
    );

    Optional<BidTemplateField>
    findByGeneratedDocumentIdAndLogicalName(
            Long generatedDocumentId,
            String logicalName
    );

    Optional<BidTemplateField>
    findByFieldUuid(
            UUID fieldUuid
    );
}