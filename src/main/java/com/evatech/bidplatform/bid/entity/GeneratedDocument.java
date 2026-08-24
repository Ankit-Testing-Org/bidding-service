package com.evatech.bidplatform.bid.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "generated_document")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String storagePath;

    @Column(nullable = false)
    private String contentType;

    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidDocumentType documentType;

    @Column(nullable = false)
    private String generatedBy;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "generatedDocument",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BidTemplateField> templateFields = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bid_id",
            nullable = false
    )
    private Bid bid;

    @PrePersist
    public void prePersist() {

        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}