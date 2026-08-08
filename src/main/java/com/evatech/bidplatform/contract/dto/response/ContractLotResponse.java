package com.evatech.bidplatform.contract.dto.response;

import com.evatech.bidplatform.contract.entity.LotQualificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractLotResponse {

    private Long id;

    private String lotNumber;

    private String lotName;

    private String description;

    private Integer startPage;

    private Integer endPage;

    private double valuation;

    private LotQualificationStatus participationStatus;

    private String selectedBy;

    private LocalDateTime selectedAt;

    private LocalDateTime createdAt;
}