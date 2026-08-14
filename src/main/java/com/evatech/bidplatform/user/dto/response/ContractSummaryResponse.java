package com.evatech.bidplatform.user.dto.response;

import com.evatech.bidplatform.contract.entity.RiskLevel;
import com.evatech.bidplatform.contract.entity.analysis.AnalysisStatus;
import com.evatech.bidplatform.contract.entity.analysis.QualificationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContractSummaryResponse(

        Long id,

        String clientName,

        BigDecimal contractValue,

        String currency,

        LocalDate submissionDeadline,

        LocalDate contractStartDate,

        LocalDate contractEndDate,

        Integer totalLots,

        Integer qualifiedLots,

        Double overallBidScore,

        RiskLevel overallRiskLevel,

        Double legalScore,

        Double complianceScore,

        Double commercialScore,

        Double penaltyScore,

        String executiveSummary,

        String recommendation,

        Integer mandatoryDocuments,

        Integer criticalClauses,

        Integer durationMonths,

        QualificationStatus qualificationStatus,

        AnalysisStatus status,

        String failureReason,

        LocalDateTime analyzedAt

) {
}