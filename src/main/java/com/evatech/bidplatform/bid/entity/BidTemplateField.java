package com.evatech.bidplatform.bid.entity;

import com.evatech.bidplatform.bid.dto.BidTemplateFieldSource;
import com.evatech.bidplatform.bid.dto.BidTemplateFieldType;
import java.util.UUID;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        name = "bid_template_field",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bid_template_field_uuid",
                        columnNames = "field_uuid"
                ),
                @UniqueConstraint(
                        name = "uk_document_logical_name",
                        columnNames = {
                                "generated_document_id",
                                "logical_name"
                        }
                )
        }
)
public class BidTemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "field_uuid",
            nullable = false,
            updatable = false
    )
    private UUID fieldUuid;

    @Column(
            name = "logical_name",
            nullable = false,
            length = 150
    )
    private String logicalName;

    @Column(
            name = "placeholder",
            nullable = false,
            length = 300
    )
    private String placeholder;

    @Column(
            name = "field_label",
            nullable = false,
            length = 500
    )
    private String fieldLabel;

    @Column(
            name = "field_description",
            columnDefinition = "TEXT"
    )
    private String fieldDescription;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "field_type",
            nullable = false,
            length = 50
    )
    private BidTemplateFieldType fieldType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "field_source",
            nullable = false,
            length = 50
    )
    private BidTemplateFieldSource source;

    @Column(
            name = "required_field",
            nullable = false
    )
    private boolean required;

    @Column(
            name = "default_value",
            columnDefinition = "TEXT"
    )
    private String defaultValue;

    @ElementCollection
    @CollectionTable(
            name = "bid_template_field_option",
            joinColumns = @JoinColumn(
                    name = "bid_template_field_id"
            )
    )
    @Column(
            name = "option_value",
            nullable = false,
            length = 500
    )
    @Builder.Default
    private List<String> allowedValues =
            new ArrayList<>();

    @Column(
            name = "section_key",
            nullable = false,
            length = 100
    )
    private String sectionKey;

    @Column(
            name = "section_title",
            nullable = false,
            length = 500
    )
    private String sectionTitle;

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;

    @Column(
            name = "entry_line_count",
            nullable = false
    )
    private Integer entryLineCount;

    @Column(
            name = "repeatable_field",
            nullable = false
    )
    private boolean repeatable;

    @Column(
            name = "repeatable_group_key",
            length = 150
    )
    private String repeatableGroupKey;

    @Column(
            name = "calculation_expression",
            length = 1000
    )
    private String calculationExpression;

    @Column(
            name = "field_value",
            columnDefinition = "TEXT"
    )
    private String fieldValue;

    @Column(
            name = "completed",
            nullable = false
    )
    private boolean completed;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "generated_document_id",
            nullable = false
    )
    private GeneratedDocument generatedDocument;

    public void updateValue(
            String value
    ) {
        this.fieldValue = value;
        this.completed =
                value != null && !value.isBlank();
    }

    public void clearValue() {
        this.fieldValue = null;
        this.completed = false;
    }
}