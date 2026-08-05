package com.evatech.bidplatform.contract.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractPageTextResponse {
    private Long id;
    private Integer pageNumber;
    private String text;
}
