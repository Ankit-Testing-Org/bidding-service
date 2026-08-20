package com.evatech.bidplatform.dashboard.dto.approval.response;

import com.evatech.bidplatform.dashboard.dto.approval.ApprovalPriority;
import com.evatech.bidplatform.dashboard.dto.approval.ApprovalStage;
import com.evatech.bidplatform.dashboard.dto.approval.ApprovalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApprovalResponse(

        Long id,

        String title,

        Long proposalId,

        String proposalNumber,

        String proposalName,

        String contractName,

        ApprovalStage approvalStage,

        ApprovalStatus status,

        ApprovalPriority priority,

        String approverUserId,

        String approverName,

        BigDecimal proposalValue,

        String currency,

        LocalDate dueDate,

        LocalDateTime createdAt

) {
}