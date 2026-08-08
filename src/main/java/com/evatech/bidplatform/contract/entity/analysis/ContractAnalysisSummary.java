package com.evatech.bidplatform.contract.entity.analysis;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.RiskLevel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract_analysis_summary")
public class ContractAnalysisSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_document_id", nullable = false)
    private ContractDocument contractDocument;

    // Contract Information

    private String clientName;

    @Column(precision = 19, scale = 2)
    private BigDecimal contractValue;

    private String currency;

    private LocalDate submissionDeadline;

    private LocalDate contractStartDate;

    private LocalDate contractEndDate;

    // Overall Analysis

    @Column(name = "total_lots")
    private Integer totalLots;

    @Column(name = "qualified_lots")
    private Integer qualifiedLots;

    @Column(name = "overall_bid_score")
    private Double overallBidScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_risk_level")
    private RiskLevel overallRiskLevel;

    // Health Score Section

    @Column(name = "legal_score")
    private Double legalScore;

    @Column(name = "compliance_score")
    private Double complianceScore;

    @Column(name = "commercial_score")
    private Double commercialScore;

    @Column(name = "penalty_score")
    private Double penaltyScore;

    // Executive Dashboard

    @Lob
    @Column(name = "executive_summary")
    private String executiveSummary;

    @Lob
    @Column(name = "recommendation")
    private String recommendation;

    @Column(name = "mandatory_documents")
    private Integer mandatoryDocuments;

    @Column(name = "critical_clauses")
    private Integer criticalClauses;

    @Column(name = "duration_months")
    private Integer durationMonths;

    // Bid Recommendation

    @Enumerated(EnumType.STRING)
    private QualificationStatus qualificationStatus;

    // Audit

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status")
    private AnalysisStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();

        if (status == null) {
            status = AnalysisStatus.COMPLETED;
        }
    }
}