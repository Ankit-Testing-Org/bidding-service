package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.entity.LotQualificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractLotRepository extends JpaRepository<ContractLot, Long> {

    List<ContractLot> findByContractDocumentIdOrderByLotNumberAsc(Long contractId);

    Optional<ContractLot> findByIdAndContractDocumentId(Long contractId, Long lotId);

    List<ContractLot> findByContractDocumentIdAndQualificationStatus(
            Long contractId,
            LotQualificationStatus qualificationStatus
    );
    List<ContractLot> findByContractDocumentIdAndQualifiedTrue(Long contractDocumentId);

}
