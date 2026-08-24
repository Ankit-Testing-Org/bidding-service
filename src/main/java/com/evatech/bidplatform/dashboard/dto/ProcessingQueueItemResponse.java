package com.evatech.bidplatform.dashboard.dto;

import com.evatech.bidplatform.contract.entity.ContractStatus;

import java.time.LocalDateTime;

public record ProcessingQueueItemResponse(

        Long contractId,

        String originalFileName,

        ContractStatus status,

        Integer progressPercentage,

        String currentStep,

        LocalDateTime uploadedAt
) {
}