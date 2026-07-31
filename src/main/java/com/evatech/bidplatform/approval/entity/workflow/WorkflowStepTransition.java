package com.evatech.bidplatform.approval.entity.workflow;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "workflow_step_transition")
public class WorkflowStepTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_step_id")
    private WorkflowStep fromStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_step_id")
    private WorkflowStep toStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowAction action;

    @Column(nullable = false)
    private Boolean completesWorkflow;
}