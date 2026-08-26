
package com.evatech.bidplatform.contract.dto.response.contract;

import com.evatech.bidplatform.contract.entity.ContractAssignmentStatus;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import lombok.Builder;

import java.time.LocalDateTime;


import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ContractResponse(

        Long id,

        String originalFileName,

        String fileType,

        Integer pageCount,

        String uploadedBy,

        LocalDateTime uploadedAt,

        ContractStatus status,

        ContractAssignmentStatus assignmentStatus,

        String assignedTo,

        String assignedBy,

        LocalDateTime assignedAt,

        String clientName,

        BigDecimal contractValue,

        String currency,

        LocalDate submissionDeadline,

        LocalDate contractStartDate,

        LocalDate contractEndDate,

        Integer estimatedBidCount

) {
}
