package com.evatech.bidplatform.contract.repository;


import com.evatech.bidplatform.contract.entity.ContractHighlight;
import com.evatech.bidplatform.contract.entity.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractHighlightRepository extends JpaRepository<ContractHighlight, Long> {

    List<ContractHighlight> findByContractDocumentId(Long contractDocumentId);

    List<ContractHighlight> findByContractDocumentIdOrderByPageNumberAsc(Long contractDocumentId);

    List<ContractHighlight> findByContractDocumentIdAndRiskLevel(
            Long contractDocumentId,
            RiskLevel riskLevel
    );

    boolean existsByContractDocumentId(Long contractDocumentId);

    void deleteByContractDocumentId(Long contractDocumentId);
}
