package com.evatech.bidplatform.approval.entity.workflow;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "workflow_task")
public class WorkflowTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_instance_id")
    private WorkflowInstance workflowInstance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_step_id")
    private WorkflowStep workflowStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WorkflowTaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssignmentType assignmentType;

    @Column(length = 100)
    private String assigneeUserId;

    @Column(length = 100)
    private String assignedBy;

    @Column(length = 100)
    private String assigneeGroupKey;

    @Column(length = 500)
    private String taskTitle;

    @Lob
    @Column(name = "task_description", columnDefinition = "LONGTEXT")
    private String taskDescription;

    @Lob
    @Column(name = "comment", columnDefinition = "LONGTEXT")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String createdBy;

    private LocalDateTime assignedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime dueAt;

    public WorkflowTask() {
    }

    public void assignToUser(String userId, String assignedBy) {

        if (status != WorkflowTaskStatus.UNASSIGNED) {
            throw new IllegalStateException(
                    "Only unassigned tasks can be assigned"
            );
        }

        this.assigneeUserId = userId;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now();
        this.status = WorkflowTaskStatus.ASSIGNED;
    }

    public void startWork() {

        if (status != WorkflowTaskStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Task must be assigned before starting"
            );
        }

        this.status = WorkflowTaskStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void approve(String comment) {

        if (!isActive()) {
            throw new IllegalStateException(
                    "Task is already completed"
            );
        }

        this.comment = comment;
        this.completedAt = LocalDateTime.now();
        this.status = WorkflowTaskStatus.COMPLETED;
    }

    public void reject(String comment) {

        if (!isActive()) {
            throw new IllegalStateException(
                    "Task is already completed"
            );
        }

        this.comment = comment;
        this.completedAt = LocalDateTime.now();
        this.status = WorkflowTaskStatus.REJECTED;
    }

    public void cancel(String comment) {

        this.comment = comment;
        this.completedAt = LocalDateTime.now();
        this.status = WorkflowTaskStatus.CANCELLED;
    }

    public boolean isAssigned() {
        return assigneeUserId != null;
    }

    public boolean isActive() {
        return status == WorkflowTaskStatus.UNASSIGNED
                || status == WorkflowTaskStatus.ASSIGNED
                || status == WorkflowTaskStatus.IN_PROGRESS;
    }
}