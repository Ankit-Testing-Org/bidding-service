package com.evatech.bidplatform.dashboard.entity;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "proposal")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String proposalNumber;

    private String title;

    private BigDecimal proposalValue;

    private LocalDate submissionDate;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private ContractDocument contractDocument;
}
