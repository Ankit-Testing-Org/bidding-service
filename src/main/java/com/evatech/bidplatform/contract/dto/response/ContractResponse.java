
package com.evatech.bidplatform.contract.dto.response;

import com.evatech.bidplatform.contract.entity.ContractAssignmentStatus;
import com.evatech.bidplatform.contract.entity.ContractStatus;
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
public class ContractResponse {

    private Long id;

    private String originalFileName;

    private String fileType;

    private Integer pageCount;

    private String uploadedBy;

    private LocalDateTime uploadedAt;

    private ContractStatus status;

    private ContractAssignmentStatus assignmentStatus;

    private String assignedTo;

    private String assignedBy;

    private LocalDateTime assignedAt;
}
