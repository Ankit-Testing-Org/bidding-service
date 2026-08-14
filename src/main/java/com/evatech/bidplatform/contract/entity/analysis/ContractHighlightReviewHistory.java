package com.evatech.bidplatform.contract.entity.analysis;

import com.evatech.bidplatform.contract.dto.HighlightReviewStatus;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract_highlight_review_history")
public class ContractHighlightReviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "contract_highlight_id",
            nullable = false
    )
    private ContractHighlight contractHighlight;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "review_status",
            nullable = false
    )
    private HighlightReviewStatus reviewStatus;

    @Column(
            name = "reviewed_by",
            nullable = false
    )
    private String reviewedBy;

    @Column(
            name = "reviewed_at",
            nullable = false
    )
    private LocalDateTime reviewedAt;

    @Lob
    @Column(name = "review_comment")
    private String reviewComment;

    @PrePersist
    public void prePersist() {

        if (reviewedAt == null) {
            reviewedAt = LocalDateTime.now();
        }
    }
}