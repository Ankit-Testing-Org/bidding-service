package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.analysis.ContractAnalysisSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractAnalysisSummaryRepository
        extends JpaRepository<ContractAnalysisSummary, Long> {

    Optional<ContractAnalysisSummary> findByContractDocumentId(
            Long contractDocumentId
    );

    void deleteByContractDocumentId(Long contractDocumentId);

    boolean existsByContractDocumentId(Long contractDocumentId);
}