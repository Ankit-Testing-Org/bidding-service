package com.evatech.bidplatform.bid.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bid_document")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bid_id",
            nullable = false
    )
    private Bid bid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidDocumentType documentType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String storagePath;

    private String contentType;

    private Long fileSize;

    private String uploadedBy;

    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {

        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}