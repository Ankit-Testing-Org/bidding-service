package com.evatech.bidplatform.contract.entity.analysis;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract_lot_qualification_history")
public class ContractLotQualificationHistory {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private ContractLotQualificationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private ContractLotQualificationStatus newStatus;

    private String changedBy;

    private String comment;

    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_lot_id", nullable = false)
    private ContractLot contractLot;
}
