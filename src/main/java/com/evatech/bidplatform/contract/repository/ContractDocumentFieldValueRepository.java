package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.ContractDocumentFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractDocumentFieldValueRepository
        extends JpaRepository<ContractDocumentFieldValue, Long> {

    List<ContractDocumentFieldValue> findByContractDocumentId(
            Long contractDocumentId);

    List<ContractDocumentFieldValue> findByTemplateFieldId(
            Long templateFieldId);

    Optional<ContractDocumentFieldValue>
    findByContractDocumentIdAndTemplateFieldId(
            Long contractDocumentId,
            Long templateFieldId);

    boolean existsByContractDocumentIdAndTemplateFieldId(
            Long contractDocumentId,
            Long templateFieldId);

    void deleteByContractDocumentId(
            Long contractDocumentId);
}
