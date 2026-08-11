package com.evatech.bidplatform.bid.entity;


import com.evatech.bidplatform.approval.entity.ApprovalHistory;
import com.evatech.bidplatform.approval.entity.ApprovalTask;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
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

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    private BidReviewStatus reviewStatus;

    @Column(name = "ai_refill_requested_by")
    private String aiRefillRequestedBy;

    @Column(name = "ai_refill_requested_at")
    private LocalDateTime aiRefillRequestedAt;

    @Column(name = "ai_refill_comment")
    private String aiRefillComment;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "uploaded_bid_file_name")
    private String uploadedBidFileName;

    @Column(name = "uploaded_bid_storage_path")
    private String uploadedBidStoragePath;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "uploaded_by")
    private String uploadedBy;

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

    @Builder.Default
    @OneToMany(
            mappedBy = "bid",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BidDocument> documents = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = BidStatus.DRAFT;
        }
    }

    public void acceptAiFilledBid(String userName) {

        this.status = BidStatus.USER_REVIEWED;

        this.reviewedBy = userName;
        this.reviewedAt = LocalDateTime.now();
        this.reviewComment = null;

        this.aiRefillRequestedBy = null;
        this.aiRefillRequestedAt = null;
        this.aiRefillComment = null;
    }

    public void requestAiRefill(
            String userName,
            String comment) {

        this.status = BidStatus.USER_REFILL_REQUESTED;

        this.aiRefillRequestedBy = userName;
        this.aiRefillRequestedAt = LocalDateTime.now();
        this.aiRefillComment = comment;
    }

    public void approveReview(String userName) {

        this.reviewStatus = BidReviewStatus.APPROVED;

        this.reviewedBy = userName;
        this.reviewedAt = LocalDateTime.now();
        this.reviewComment = null;
    }

    public void requestReanalysis(
            String userName,
            String comment) {

        this.reviewStatus = BidReviewStatus.REANALYSIS_REQUESTED;

        this.reviewComment = comment;
    }

    public void rejectReview(
            String userName,
            String comment) {

        this.reviewStatus = BidReviewStatus.REJECTED;

        this.reviewComment = comment;
        this.reviewedBy = userName;
        this.reviewedAt = LocalDateTime.now();
    }
}