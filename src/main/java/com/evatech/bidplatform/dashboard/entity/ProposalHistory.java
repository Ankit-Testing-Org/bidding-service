package com.evatech.bidplatform.dashboard.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "proposal_history")
public class ProposalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @Enumerated(EnumType.STRING)
    private ProposalStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private ProposalStatus newStatus;

    private String actionBy;

    private LocalDateTime actionAt;

    @Column(length = 2000)
    private String comment;
}