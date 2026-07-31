package com.evatech.bidplatform.approval.entity.workflow;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "workflow_step")
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id")
    private WorkflowDefinition workflowDefinition;

    @Column(nullable = false)
    private String stepKey;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentType assignmentType;

    @Column(nullable = false)
    private String assigneeGroupKey;

    @Column(nullable = false)
    private Boolean pickRequired;

    @Column(nullable = false)
    private Boolean finalStep;
}