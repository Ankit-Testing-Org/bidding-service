package com.evatech.bidplatform.dashboard.dto.summary.response;


public record MySummaryDashboardResponse(

        MyProfileResponse profile,
        MySummaryStatsResponse response
){
}