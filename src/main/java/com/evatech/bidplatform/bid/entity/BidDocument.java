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
    @JoinColumn(name = "bid_id")
    private Bid bid;

    private String fileName;

    private String storagePath;

    @Enumerated(EnumType.STRING)
    private BidDocumentType documentType;

    private String uploadedBy;

    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        uploadedAt = LocalDateTime.now();
    }
}