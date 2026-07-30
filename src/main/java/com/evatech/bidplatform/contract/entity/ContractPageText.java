package com.evatech.bidplatform.contract.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "contract_page_text",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_contract_page_text_contract_page",
                        columnNames = {"contract_document_id", "page_number"}
                )
        }
)
public class ContractPageText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Lob
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_document_id", nullable = false)
    private ContractDocument contractDocument;
}