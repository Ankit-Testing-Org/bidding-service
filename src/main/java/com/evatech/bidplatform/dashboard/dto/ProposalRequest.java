package com.evatech.bidplatform.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ProposalRequest(
        @NotBlank
        String title,
        LocalDate submissionDate,
        @NotNull
        Long contractDocumentId,
        List<Long> contractLotIds

) {
}