package com.evatech.bidplatform.dashboard.dto.contract.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContractListItemResponse(
        Long contractId,
        String contractName,
        String clientName,
        String status,
        String assignmentStatus,
        String assignedTo,
        String assignedToName,
        String stage,
        BigDecimal valuation,
        String currency,
        Integer lotCount,
        Integer qualifiedLotCount,
        LocalDateTime uploadedAt,
        String pdfUrl
) {
}