package com.evatech.bidplatform.contract.entity.analysis;

import com.evatech.bidplatform.contract.entity.ContractLot;
import com.evatech.bidplatform.contract.entity.RiskLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "contract_lot_analysis")
public class ContractLotAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_lot_id",
            nullable = false,
            unique = true
    )
    private ContractLot contractLot;

    private Boolean recommendedToBid;

    private Double winProbability;

    @Enumerated(EnumType.STRING)
    private RiskLevel overallRiskLevel;

    @Lob
    private String executiveSummary;

    @Lob
    private String recommendation;
}