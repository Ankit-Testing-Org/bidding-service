package com.evatech.bidplatform.dashboard.dto.approval.response;

import com.evatech.bidplatform.approval.entity.ApprovalStatus;

import java.time.LocalDateTime;

public record ApprovalDecisionResponse(

        Long approvalId,

        ApprovalStatus status,

        String assignedUserId,

        String assignedUserName,

        String message,

        LocalDateTime updatedAt

) {
}