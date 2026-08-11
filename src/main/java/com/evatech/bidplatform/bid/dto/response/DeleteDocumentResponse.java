package com.evatech.bidplatform.bid.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteDocumentResponse {

    private Long documentId;
    private String fileName;
    private String message;
}