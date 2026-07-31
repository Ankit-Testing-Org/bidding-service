package com.evatech.bidplatform.approval.entity.workflow;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "workflow_instance")
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id")
    private WorkflowDefinition workflowDefinition;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "current_step_id")
    private WorkflowStep currentStep;

    @Column(nullable = false)
    private String businessType;

    @Column(nullable = false)
    private Long businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowInstanceStatus status;

    @Column(nullable = false)
    private String startedBy;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    protected WorkflowInstance() {
    }

    public WorkflowInstance(
            WorkflowDefinition workflowDefinition,
            WorkflowStep currentStep,
            String businessType,
            Long businessId,
            String startedBy
    ) {
        this.workflowDefinition = workflowDefinition;
        this.currentStep = currentStep;
        this.businessType = businessType;
        this.businessId = businessId;
        this.status = WorkflowInstanceStatus.IN_PROGRESS;
        this.startedBy = startedBy;
        this.startedAt = LocalDateTime.now();
    }

    public void moveToStep(WorkflowStep nextStep) {
        this.currentStep = nextStep;
        this.status = WorkflowInstanceStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = WorkflowInstanceStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = WorkflowInstanceStatus.REJECTED;
        this.completedAt = LocalDateTime.now();
    }
}