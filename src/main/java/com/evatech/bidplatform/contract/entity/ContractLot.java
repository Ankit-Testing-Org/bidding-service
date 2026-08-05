package com.evatech.bidplatform.contract.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_status", nullable = false)
    private LotParticipationStatus participationStatus;

    @Column(name = "selected_by")
    private String selectedBy;

    @Column(name = "selected_at")
    private LocalDateTime selectedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.participationStatus == null) {
            this.participationStatus = LotParticipationStatus.OPEN;
        }
    }

    public void selectForBid(String selectedBy) {
        this.participationStatus =
                LotParticipationStatus.SELECTED_FOR_BID;
        this.selectedBy = selectedBy;
        this.selectedAt = LocalDateTime.now();
    }

    public void markNotApplicable(String selectedBy) {
        this.participationStatus =
                LotParticipationStatus.NOT_APPLICABLE;
        this.selectedBy = selectedBy;
        this.selectedAt = LocalDateTime.now();
    }

    public void reopen() {
        this.participationStatus =
                LotParticipationStatus.OPEN;
        this.selectedBy = null;
        this.selectedAt = null;
    }

    public boolean isSelectedForBid() {
        return LotParticipationStatus.SELECTED_FOR_BID
                .equals(this.participationStatus);
    }

    public boolean isNotApplicable() {
        return LotParticipationStatus.NOT_APPLICABLE
                .equals(this.participationStatus);
    }
}