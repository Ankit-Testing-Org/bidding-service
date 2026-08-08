package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.analysis.ContractLotAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractLotAnalysisRepository extends JpaRepository<ContractLotAnalysis, Long> {

    boolean existsByContractLotId(Long contractLotId);

    Optional<ContractLotAnalysis> findByContractLotId(Long contractLotId);

    void deleteByContractLotId(Long contractLotId);
}
