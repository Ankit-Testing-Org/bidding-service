package com.evatech.bidplatform.bid.entity;


import com.evatech.bidplatform.approval.entity.ApprovalHistory;
import com.evatech.bidplatform.approval.entity.ApprovalTask;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractLot;
import com.evatech.bidplatform.document.entity.GeneratedDocument;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bid")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bid_reference_number", nullable = false, unique = true)
    private String bidReferenceNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "current_owner")
    private String currentOwner;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BidStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_document_id", nullable = false)
    private ContractDocument contractDocument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_lot_id", nullable = false)
    private ContractLot contractLot;

    @Builder.Default
    @OneToMany(
            mappedBy = "bid",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BidField> fields = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "bid",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ApprovalTask> approvalTasks = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "bid",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ApprovalHistory> approvalHistories = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "bid",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<GeneratedDocument> generatedDocuments = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = BidStatus.DRAFT;
        }
    }

    public void markSubmitted() {
        this.status = BidStatus.SUBMITTED_FOR_APPROVAL;
        this.submittedAt = LocalDateTime.now();
    }
}