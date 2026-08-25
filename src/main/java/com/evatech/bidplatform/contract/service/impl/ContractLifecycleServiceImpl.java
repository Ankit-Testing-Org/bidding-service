package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.audit.service.AuditAction;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import com.evatech.bidplatform.contract.entity.analysis.AnalysisStatus;
import com.evatech.bidplatform.contract.entity.analysis.ContractAnalysisSummary;
import com.evatech.bidplatform.contract.repository.ContractAnalysisSummaryRepository;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import com.evatech.bidplatform.contract.service.ContractAccessService;
import com.evatech.bidplatform.contract.service.ContractLifecycleService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractLifecycleServiceImpl
        implements ContractLifecycleService {

    private final ContractDocumentRepository contractDocumentRepository;
    private final ContractAnalysisSummaryRepository contractAnalysisSummaryRepository;
    private final ContractAccessService contractAccessService;

    @AuditAction(action = "MARK_ANALYSIS_IN_PROGRESS", entity = "ContractDocument")
    @Override
    public ContractDocument markAnalysisInProgress(
            Long contractId,
            User user,
            List<String> roles
    ) {
        ContractDocument contractDocument =
                contractAccessService.getAccessibleContract(
                        contractId,
                        user,
                        roles
                );

        ContractStatus currentStatus = contractDocument.getStatus();

        if (!ContractStatus.TEXT_EXTRACTED.equals(currentStatus)
                && !ContractStatus.ANALYSED.equals(currentStatus)) {
            throw new IllegalStateException(
                    "Contract text must be extracted before "
                            + "analysis can start"
            );
        }

        contractDocument.setStatus(
                ContractStatus.ANALYSIS_IN_PROGRESS
        );

        return contractDocumentRepository.save(contractDocument);
    }

    @AuditAction(action = "MARK_ANALYSED", entity = "ContractDocument")
    @Override
    public ContractDocument markAnalysed(
            Long contractId,
            User user,
            List<String> roles
    ) {
        ContractDocument contractDocument =
                contractAccessService.getAccessibleContract(
                        contractId,
                        user,
                        roles
                );

        if (!ContractStatus.ANALYSIS_IN_PROGRESS.equals(
                contractDocument.getStatus()
        )) {
            throw new IllegalStateException(
                    "Contract must be in analysis progress "
                            + "before marking as analysed"
            );
        }

        contractDocument.setStatus(ContractStatus.ANALYSED);

        return contractDocumentRepository.save(contractDocument);
    }

    @Override
    public void markAnalysisFailed(
            Long contractId,
            String failureReason,
            User user,
            List<String> roles
    ) {
        ContractDocument contractDocument =
                contractAccessService.getAccessibleContract(
                        contractId,
                        user,
                        roles
                );

        ContractAnalysisSummary summary =
                contractAnalysisSummaryRepository
                        .findByContractDocumentId(contractId)
                        .orElseGet(() ->
                                ContractAnalysisSummary.builder()
                                        .contractDocument(contractDocument)
                                        .build()
                        );

        summary.setStatus(AnalysisStatus.FAILED);
        summary.setFailureReason(failureReason);
        summary.setAnalyzedAt(LocalDateTime.now());

        contractAnalysisSummaryRepository.save(summary);

        contractDocument.setStatus(ContractStatus.FAILED);
        contractDocumentRepository.save(contractDocument);
    }

    @Override
    public ContractDocument updateContractDocumentStatus(Long contractId,
                                                         ContractDocument contract,
                                                         ContractStatus contractStatus,
                                                         User user, List<String> roles) {
        ContractDocument contractDocument = contract;
        if(contract == null) {
            contractDocument = contractAccessService.getAccessibleContract(contractId,user,roles);
        }
        contractDocument.setStatus(contractStatus);
        return contractDocumentRepository.save(contractDocument);
    }
}