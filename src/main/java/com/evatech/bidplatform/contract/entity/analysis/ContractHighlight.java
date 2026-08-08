package com.evatech.bidplatform.contract.entity.analysis;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.RiskLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract_highlight")
public class ContractHighlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ContractHighlightCategory category;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "page_number")
    private Integer pageNumber;

    /**
     * Example:
     * Clause 7.3
     * Section 5.4
     * Annexure B
     */
    @Column(name = "reference")
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    /**
     * 0 - 100
     * Used for sorting critical findings
     */
    @Column(name = "severity_score")
    private Integer severityScore;

    @Lob
    @Column(name = "recommended_action")
    private String recommendedAction;

    @Column(name = "mandatory")
    private Boolean mandatory;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_document_id", nullable = false)
    private ContractDocument contractDocument;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();

        if (mandatory == null) {
            mandatory = false;
        }

        if (severityScore == null) {
            severityScore = 50;
        }
    }
}