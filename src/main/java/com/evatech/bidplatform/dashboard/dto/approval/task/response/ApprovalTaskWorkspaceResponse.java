package com.evatech.bidplatform.dashboard.dto.approval.task.response;

import java.util.List;

public record ApprovalTaskWorkspaceResponse(

        Long taskId,

        Long workflowId,

        Long stageId,

        String stageName,

        String taskStatus,

        String approvalType,

        Long contractId,

        String contractNumber,

        String contractTitle,

        String assignedTo,

        String assignedGroup,

        boolean canAssignToSelf,

        boolean canApprove,

        boolean alreadyDecided,

        PreparedBidDocumentResponse preparedBidDocument,

        List<ContractHighlightResponse> highlights,

        List<LotHighlightResponse> lotHighlights,

        ProposalApprovalSummaryResponse proposal,

        List<ApprovalHistoryResponse> history

) {
}