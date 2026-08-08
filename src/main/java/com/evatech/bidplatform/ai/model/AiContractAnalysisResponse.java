package com.evatech.bidplatform.ai.model;

import com.evatech.bidplatform.contract.entity.RiskLevel;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlight;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AiContractAnalysisResponse(

        String clientName,

        BigDecimal contractValue,

        String currency,

        LocalDate submissionDeadline,

        LocalDate contractStartDate,

        LocalDate contractEndDate,

        String executiveSummary,

        String recommendation,

        Double overallBidScore,

        RiskLevel overallRiskLevel,

        Double legalScore,

        Double complianceScore,

        Double commercialScore,

        Double penaltyScore,

        Integer criticalClauses,

        Integer mandatoryDocuments,

        Integer durationMonths,

        Integer totalLots,

        Integer qualifiedLots,

        List<ContractHighlight> highlights

) {
}