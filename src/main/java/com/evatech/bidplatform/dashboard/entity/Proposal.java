package com.evatech.bidplatform.dashboard.entity;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_document_id")
    private ContractDocument contractDocument;

    @OneToMany(
            mappedBy = "proposal",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProposalHistory> history =
            new ArrayList<>();
}
