package com.evatech.bidplatform.approval.entity.workflow;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "workflow_definition")
public class WorkflowDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String workflowKey;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private Boolean active;
}