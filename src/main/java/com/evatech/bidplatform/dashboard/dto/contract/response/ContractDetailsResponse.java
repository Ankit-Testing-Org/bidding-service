package com.evatech.bidplatform.dashboard.dto.contract.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContractDetailsResponse(
        Long contractId,
        String contractName,
        String clientName,
        String fileName,
        String fileType,
        Integer pageCount,
        String status,
        String assignmentStatus,
        String assignedTo,
        String assignedToName,
        LocalDateTime assignedAt,
        String uploadedBy,
        LocalDateTime uploadedAt,
        BigDecimal contractValue,
        String currency,
        LocalDate submissionDeadline,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        Integer estimatedBidCount,
        Integer lotCount,
        Integer qualifiedLotCount,
        Integer highlightCount,
        Boolean bidCreated,
        String pdfUrl
) {
}