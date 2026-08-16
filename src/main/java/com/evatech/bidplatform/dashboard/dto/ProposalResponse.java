package com.evatech.bidplatform.dashboard.dto;

import com.evatech.bidplatform.dashboard.entity.ProposalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProposalResponse(

        Long id,

        String proposalNumber,

        String title,

        BigDecimal proposalValue,

        Integer totalLots,

        Integer selectedLots,

        LocalDate submissionDate,

        String createdBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        ProposalStatus status,

        Long contractDocumentId,

        String contractDocumentName

) {
}