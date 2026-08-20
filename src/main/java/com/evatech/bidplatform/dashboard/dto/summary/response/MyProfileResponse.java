package com.evatech.bidplatform.dashboard.dto.summary.response;

public record MyProfileResponse(

        Long userId,

        String username,

        String displayName,

        String email,

        String role,

        String location,

        String department,

        String initials

) {
}
