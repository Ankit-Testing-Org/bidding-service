package com.evatech.bidplatform.contract.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractAnalysisOrchestrator {

    private final ContractLifecycleService contractLifecycleService;
    private final ContractTextExtractionService contractTextExtractionService;
    private final ContractHighlightService contractHighlightService;
    private final EmailService emailService;
    private final ContractLotService contractLotService;

    @Async
    @Transactional
    public void startAnalysis(ContractDocument contract,
                              User user,
                              List<String> roles) {
        try {
            contract.setProcessingStartedAt(LocalDateTime.now());
            contractLifecycleService.updateContractDocumentStatus(contract.getId(), contract,
                    ContractStatus.ANALYSIS_IN_PROGRESS, user, roles);

            processContract(contract, user, roles);

            contract.setProcessingCompletedAt(LocalDateTime.now());
            contractLifecycleService.updateContractDocumentStatus(contract.getId(), contract,
                    ContractStatus.ANALYSED, user, roles);

            sendSuccessEmail(contract);

        } catch (Exception ex) {
            log.error("Contract analysis failed for {}", contract.getId(), ex);
            contract.setProcessingError(ExceptionUtils.getRootCauseMessage(ex));
            contract.setProcessingCompletedAt(LocalDateTime.now());
            contractLifecycleService.updateContractDocumentStatus(contract.getId(), contract,
                    ContractStatus.FAILED, user, roles);

            sendFailureEmail(contract, ex);
        }
    }

    private void sendFailureEmail(ContractDocument contract, Exception ex) {
        emailService.sendEmail(new ArrayList<>(),
                "New contract failed to upload",
                """
                A new contract is failed to upload , with error:
                
                contract id %s , contract name %s and error %s
                """.formatted(contract.getId(),
                        contract.getOriginalFileName(),
                        ex.getMessage()));

    }

    private void sendSuccessEmail(ContractDocument contract) {
        emailService.sendEmail(new ArrayList<>(),
                "New contract is upload",
                """
                A new contract is being uploaded , please assign it:
                
                contract id %s and contract name %s
                """.formatted(contract.getId(),
                        contract.getOriginalFileName()));
    }

    private void processContract(ContractDocument contractDocument, User user,
                                 List<String> roles) {
        contractTextExtractionService.extractText(contractDocument.getId(), user, roles);

        List<ContractLot> contractLots = contractLotService.processExtractingLots(contractDocument, user);

        contractHighlightService.analyseContractHighlights(contractDocument.getId(),
                false, user, roles);

        contractLots.forEach(contractLot -> contractLotService.analyseContractLot(contractLot.getId(),
                false, user, roles , null));
    }
}
