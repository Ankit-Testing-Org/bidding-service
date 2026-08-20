package com.evatech.bidplatform.dashboard.dto.approval.response;

public record DelegateUserResponse(

        String userId,

        String fullName,

        String email,

        String role

) {
}
