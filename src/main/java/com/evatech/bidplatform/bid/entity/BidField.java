package com.evatech.bidplatform.bid.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "bid_field",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_bid_field_name_per_bid",
                        columnNames = {"bid_id", "field_name"}
                )
        }
)
public class BidField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Lob
    @Column(name = "field_value")
    private String fieldValue;

    @Column(name = "source_page_number")
    private Integer sourcePageNumber;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "manually_edited", nullable = false)
    private Boolean manuallyEdited;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bid_id", nullable = false)
    private Bid bid;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.manuallyEdited == null) {
            this.manuallyEdited = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateValue(String fieldValue) {
        this.fieldValue = fieldValue;
        this.manuallyEdited = true;
        this.updatedAt = LocalDateTime.now();
    }
}