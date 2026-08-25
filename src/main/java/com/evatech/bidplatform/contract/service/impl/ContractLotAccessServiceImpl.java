package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.audit.service.AuditAction;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.contract.repository.ContractLotRepository;
import com.evatech.bidplatform.contract.service.ContractAccessService;
import com.evatech.bidplatform.contract.service.ContractLotAccessService;
import com.evatech.bidplatform.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ContractLotAccessServiceImpl implements ContractLotAccessService {

    private final ContractAccessService contractAccessService;
    private final ContractLotRepository contractLotRepository;

    @AuditAction(action = "FETCH_CONTRACT_LOTS", entity = "ContractLot")
    @Override
    @Transactional
    public List<ContractLot> getContractLots(
            Long contractId,
            User user,
            List<String> roles
    ) {
        contractAccessService.getAccessibleContract(
                contractId,
                user,
                roles
        );

        return contractLotRepository
                .findByContractDocumentIdOrderByLotNumberAsc(
                        contractId
                );
    }
}
