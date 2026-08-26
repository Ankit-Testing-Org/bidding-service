package com.evatech.bidplatform.contract.dto.response.highlight;

public enum HighlightReviewStatus {
    PENDING,        // AI generated, not reviewed
    APPROVED,       // User confirms AI is correct
    REJECTED,       // User says AI is wrong
    REANALYSE       // User requests AI re-analysis
}