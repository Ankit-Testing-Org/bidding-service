package com.evatech.bidplatform.dashboard.dto.approval.task.response;

public record LotHighlightResponse(

        Long lotId,

        String lotNumber,

        String lotName,

        boolean qualified,

        String qualificationStatus,

        String summary

) {
}