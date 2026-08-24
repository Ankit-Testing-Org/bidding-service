package com.evatech.bidplatform.bid.entity;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bid")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "bid_reference_number",
            nullable = false,
            unique = true
    )
    private String bidReferenceNumber;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus status;

    @Enumerated(EnumType.STRING)
    private BidReviewStatus reviewStatus;

    @Column(nullable = false)
    private String createdBy;

    private String currentOwner;

    private LocalDateTime createdAt;

    private LocalDateTime submittedAt;

    private String submittedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "contract_document_id",
            nullable = false
    )
    private ContractDocument contractDocument;

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

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = BidStatus.DRAFT;
        }

        if (reviewStatus == null) {
            reviewStatus = BidReviewStatus.PENDING;
        }
    }
}