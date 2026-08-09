package com.evatech.bidplatform.contract.entity.analysis;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "contract_lot_analysis_review")
public class ContractLotAnalysisReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_lot_id")
    private ContractLot contractLot;

    @Enumerated(EnumType.STRING)
    private LotAnalysisReviewStatus status;

    @Lob
    private String comment;

    private String reviewedBy;

    private LocalDateTime reviewedAt;
}