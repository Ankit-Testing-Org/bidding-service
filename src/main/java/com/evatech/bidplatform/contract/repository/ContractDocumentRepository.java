package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.ContractAssignmentStatus;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractDocumentRepository extends JpaRepository<ContractDocument, Long>,
        JpaSpecificationExecutor<ContractDocument> {

    List<ContractDocument> findByAssignmentStatus(
            ContractAssignmentStatus assignmentStatus
    );

    List<ContractDocument> findByAssignedToAndAssignmentStatus(
            String assignedTo,
            ContractAssignmentStatus assignmentStatus
    );

    List<ContractDocument> findAllByOrderByUploadedAtDesc();
}
