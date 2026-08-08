package com.evatech.bidplatform.contract.entity.analysis;

import com.evatech.bidplatform.contract.entity.ContractLot;
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
@Table(name = "contract_lot_highlight")
public class ContractLotHighlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_lot_id", nullable = false)
    private ContractLot contractLot;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private LotHighlightCategory category;

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

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Lob
    @Column(name = "recommended_action")
    private String recommendedAction;

    @Column(name = "is_bid_capable")
    private Boolean bidCapable;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}