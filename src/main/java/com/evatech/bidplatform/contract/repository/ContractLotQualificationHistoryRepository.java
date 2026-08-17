package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.analysis.ContractLotQualificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractLotQualificationHistoryRepository
        extends JpaRepository<ContractLotQualificationHistory, Long> {
}
