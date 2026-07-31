package com.evatech.bidplatform.contract.entity;

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

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Lob
    @Column(name = "recommended_action")
    private String recommendedAction;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_document_id", nullable = false)
    private ContractDocument contractDocument;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}