package com.evatech.bidplatform.contract.entity.analysis;

import com.evatech.bidplatform.contract.dto.LotAnalysisStatus;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.LotQualificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract_lot")
public class ContractLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractDocument contractDocument;

    @Column(name = "lot_number", nullable = false)
    private String lotNumber;

    @Column(name = "lot_name", nullable = false)
    private String lotName;

    @Column(name = "description", length = 10000)
    private String description;

    @Column(name = "start_page")
    private Integer startPage;

    @Column(name = "end_page")
    private Integer endPage;

    @Column(name = "valuation")
    private BigDecimal valuation;

    @Enumerated(EnumType.STRING)
    @Column(name = "qualification_status", nullable = false)
    private LotQualificationStatus qualificationStatus;

    @Column(name = "selected_by")
    private String selectedBy;

    @Column(name = "selected_at")
    private LocalDateTime selectedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "qualified_by")
    private String qualifiedBy;

    @Column(name = "qualified_at")
    private LocalDateTime qualifiedAt;

    @Column(name = "qualification_comment")
    private String qualificationComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false)
    private LotAnalysisStatus analysisStatus;

    @Column(name = "analysis_started_at")
    private LocalDateTime analysisStartedAt;

    @Column(name = "analysed_at")
    private LocalDateTime analysedAt;

    @Column(name = "analysis_failed_at")
    private LocalDateTime analysisFailedAt;

    @Lob
    @Column(name = "analysis_error")
    private String analysisError;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_review_status")
    private LotAnalysisReviewStatus analysisReviewStatus;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Lob
    @Column(name = "review_comment")
    private String reviewComment;

    @Column(name = "reanalysis_requested_count")
    private Integer reanalysisRequestedCount;

    @Builder.Default
    @OneToMany(mappedBy = "contractLot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractLotHighlight> highlights = new ArrayList<>();

    @OneToOne(mappedBy = "contractLot", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContractLotAnalysis analysis;

    @OneToOne(mappedBy = "contractLot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractLotQualificationHistory> history = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        if (analysisStatus == null) {
            analysisStatus = LotAnalysisStatus.NOT_ANALYSED;
        }

        if (analysisReviewStatus == null) {
            analysisReviewStatus = LotAnalysisReviewStatus.PENDING_REVIEW;
        }

        if (reanalysisRequestedCount == null) {
            reanalysisRequestedCount = 0;
        }
    }

    public void approveAnalysis(String userName, String comment) {
        this.analysisReviewStatus = LotAnalysisReviewStatus.APPROVED;
        this.reviewComment = comment;
        this.reviewedBy = userName;
        this.reviewedAt = LocalDateTime.now();
    }

    public void rejectAnalysis(String userName, String comment) {
        this.analysisReviewStatus = LotAnalysisReviewStatus.REJECTED;
        this.reviewComment = comment;
        this.reviewedBy = userName;
        this.reviewedAt = LocalDateTime.now();
    }

    public void requestReanalysis(String userName, String comment) {
        this.analysisReviewStatus = LotAnalysisReviewStatus.REANALYSIS_REQUESTED;
        this.reviewComment = comment;
        this.reviewedBy = userName;
        this.reviewedAt = LocalDateTime.now();
        this.reanalysisRequestedCount++;
    }

    public void markAnalysisInProgress() {
        this.analysisStatus = LotAnalysisStatus.ANALYSIS_IN_PROGRESS;
        this.analysisStartedAt = LocalDateTime.now();
        this.analysisFailedAt = null;
        this.analysisError = null;
    }

    public void markAnalysed() {
        this.analysisStatus = LotAnalysisStatus.ANALYSED;
        this.analysedAt = LocalDateTime.now();
        this.analysisFailedAt = null;
        this.analysisError = null;
    }

    public void markAnalysisFailed(String errorMessage) {
        this.analysisStatus = LotAnalysisStatus.ANALYSIS_FAILED;
        this.analysisFailedAt = LocalDateTime.now();
        this.analysisError = errorMessage;
    }

    public void qualify(String qualifiedBy) {
        this.qualificationStatus = LotQualificationStatus.QUALIFIED;
        this.qualifiedBy = qualifiedBy;
        this.qualifiedAt = LocalDateTime.now();
    }

    public void unqualify(String qualifiedBy) {
        this.qualificationStatus = LotQualificationStatus.UNQUALIFIED;
        this.qualifiedBy = qualifiedBy;
        this.qualifiedAt = LocalDateTime.now();
    }

    public void reopen() {
        this.qualificationStatus = LotQualificationStatus.PENDING;
        this.qualifiedBy = null;
        this.qualifiedAt = null;
        this.qualificationComment = null;
    }

    public boolean isQualified() {
        return LotQualificationStatus.QUALIFIED.equals(this.qualificationStatus);
    }

    public boolean isUnqualified() {
        return LotQualificationStatus.UNQUALIFIED.equals(this.qualificationStatus);
    }
}