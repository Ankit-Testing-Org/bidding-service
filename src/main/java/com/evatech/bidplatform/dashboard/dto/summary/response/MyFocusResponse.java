package com.evatech.bidplatform.dashboard.dto.summary.response;

import java.util.List;

public record MyFocusResponse(

        List<MyFocusItemResponse> focusItems,

        MyProgressResponse progress,

        List<MyDeadlineResponse> deadlines

) {
}