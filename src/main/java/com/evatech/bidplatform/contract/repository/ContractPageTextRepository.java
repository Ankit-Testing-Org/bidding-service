package com.evatech.bidplatform.contract.repository;

import com.evatech.bidplatform.contract.entity.ContractPageText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractPageTextRepository extends JpaRepository<ContractPageText, Long> {

    List<ContractPageText> findByContractDocumentIdOrderByPageNumberAsc(Long contractDocumentId);

    List<ContractPageText> findByContractDocumentIdAndPageNumberBetweenOrderByPageNumberAsc(
            Long contractDocumentId,
            Integer startPage,
            Integer endPage
    );

    Optional<ContractPageText> findByContractDocumentIdAndPageNumber(Long contractDocumentId, Integer pageNumber);

    void deleteByContractDocumentId(Long contractDocumentId);

}

