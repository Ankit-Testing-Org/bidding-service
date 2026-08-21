package com.evatech.bidplatform.dashboard.dto.approval.task.response;

public record EligibleApproverResponse(

        Long userId,

        String userName,

        String email,

        String role

) {
}