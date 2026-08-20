package com.evatech.bidplatform.dashboard.dto.approval.response;

import com.evatech.bidplatform.approval.entity.ApprovalStatus;

import java.time.LocalDateTime;

public record ApprovalChainResponse(

        Long id,

        String stageName,

        String approverName,

        ApprovalStatus status,

        LocalDateTime actionDate,

        String comment

) {
}