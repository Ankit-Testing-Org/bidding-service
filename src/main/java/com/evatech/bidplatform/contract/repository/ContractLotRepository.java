package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.ContractLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractLotRepository extends JpaRepository<ContractLot, Long> {

    List<ContractLot> findByContractDocumentIdOrderByLotNumberAsc(Long contractId);

    List<ContractLot> findByContractDocumentIdAndLotNumber(Long contractId, String lotNumber);

}
