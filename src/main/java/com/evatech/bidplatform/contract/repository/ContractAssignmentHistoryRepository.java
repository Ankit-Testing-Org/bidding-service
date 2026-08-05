package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.ContractAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractAssignmentHistoryRepository
        extends JpaRepository<ContractAssignmentHistory, Long> {

    List<ContractAssignmentHistory> findByContractDocumentIdOrderByAssignedAtDesc(
            Long contractId
    );
}