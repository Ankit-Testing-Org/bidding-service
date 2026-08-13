package com.evatech.bidplatform.bid.entity;


import com.evatech.bidplatform.contract.entity.ContractDocument;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "generated_document")
public class GeneratedDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "generated_by", nullable = false)
    private String generatedBy;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private ContractDocument contract;

    @PrePersist
    public void prePersist() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }

    @OneToMany(mappedBy = "generatedDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BidTemplateField> templateFields = new ArrayList<>();

    public void addTemplateField(BidTemplateField field) {
        templateFields.add(field);
        field.setGeneratedDocument(this);
    }

    public void removeTemplateField(BidTemplateField field) {
        templateFields.remove(field);
        field.setGeneratedDocument(null);
    }

}
