package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.bid.entity.BidTemplateField;
import com.evatech.bidplatform.contract.entity.ContractDocumentFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractDocumentFieldValueRepository
        extends JpaRepository<ContractDocumentFieldValue, Long> {

    Optional<ContractDocumentFieldValue> findByTemplateFieldId(BidTemplateField templateField);
}