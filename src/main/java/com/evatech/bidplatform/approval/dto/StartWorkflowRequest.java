package com.evatech.bidplatform.approval.dto;

public record StartWorkflowRequest(
        String workflowKey,
        String businessType,
        Long businessId
) {
}