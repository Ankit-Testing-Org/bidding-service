package com.evatech.bidplatform.dashboard.dto.approval.response;

import com.evatech.bidplatform.approval.entity.ApprovalStage;
import com.evatech.bidplatform.approval.entity.ApprovalStatus;
import com.evatech.bidplatform.dashboard.dto.approval.ApprovalPriority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ApprovalDetailResponse(

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

        Integer expectedMargin,

        Integer qualifiedLotCount,

        Integer riskScore,

        Integer commercialRisks,

        Integer legalRisks,

        Integer complianceRisks,

        LocalDate dueDate,

        String proposalPageUrl,

        String reviewPageUrl,

        String documentDownloadUrl,

        String context,

        List<ApprovalChainResponse> approvalChain,

        List<ReviewerCommentResponse> reviewerComments,

        List<ApprovalHistoryResponse> history

) {
}
