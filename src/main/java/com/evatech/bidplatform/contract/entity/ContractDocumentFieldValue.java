package com.evatech.bidplatform.contract.entity;

import com.evatech.bidplatform.bid.entity.BidTemplateField;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_document_field_value")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDocumentFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_document_id")
    private ContractDocument contractDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_field_id")
    private BidTemplateField templateField;

    @Column(columnDefinition = "TEXT")
    private String value;

    private String updatedBy;

    private LocalDateTime updatedAt;
}