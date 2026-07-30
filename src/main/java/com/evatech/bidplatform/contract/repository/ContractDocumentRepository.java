package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.ContractAssignmentStatus;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractDocumentRepository extends JpaRepository<ContractDocument, Long> {

    List<ContractDocument> findByUploadedBy(String uploadedBy);

    List<ContractDocument> findByStatus(ContractStatus status);

    List<ContractDocument> findByAssignmentStatus(
            ContractAssignmentStatus assignmentStatus
    );

    List<ContractDocument> findByAssignedTo(String assignedTo);

    List<ContractDocument> findByAssignedToAndAssignmentStatus(
            String assignedTo,
            ContractAssignmentStatus assignmentStatus
    );

    boolean existsByStoragePath(String storagePath);
}
