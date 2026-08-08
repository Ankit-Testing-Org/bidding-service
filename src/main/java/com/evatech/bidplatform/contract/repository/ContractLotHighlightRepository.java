package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.analysis.ContractLotHighlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractLotHighlightRepository
        extends JpaRepository<ContractLotHighlight, Long> {

    List<ContractLotHighlight> findByContractLotIdOrderByPageNumberAsc(
            Long contractLotId
    );

    boolean existsByContractLotId(Long contractLotId);
    void deleteByContractLotId(Long contractLotId);
}
