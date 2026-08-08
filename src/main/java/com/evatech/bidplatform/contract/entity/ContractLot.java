package com.evatech.bidplatform.contract.entity;

import com.evatech.bidplatform.contract.dto.LotAnalysisStatus;
import com.evatech.bidplatform.contract.entity.analysis.ContractLotAnalysis;
import com.evatech.bidplatform.contract.entity.analysis.ContractLotHighlight;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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
    private double valuation;

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

    @Builder.Default
    @OneToMany(mappedBy = "contractLot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractLotHighlight> highlights = new ArrayList<>();

    @OneToOne(mappedBy = "contractLot", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContractLotAnalysis analysis;

    @PrePersist
    public void prePersist() {
        if (this.qualificationStatus == null) {
            this.qualificationStatus = LotQualificationStatus.PENDING;
        }

        if (this.analysisStatus == null) {
            this.analysisStatus = LotAnalysisStatus.NOT_ANALYSED;
        }
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