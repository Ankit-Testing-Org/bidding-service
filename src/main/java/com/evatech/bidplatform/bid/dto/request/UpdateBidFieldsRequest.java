package com.evatech.bidplatform.bid.dto.request;

import java.util.Map;

public record UpdateBidFieldsRequest(
        Map<String, String> fields
) {
}