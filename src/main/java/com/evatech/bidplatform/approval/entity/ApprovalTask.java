package com.evatech.bidplatform.approval.entity;

import com.evatech.bidplatform.bid.entity.Bid;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "approval_task")
public class ApprovalTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private ApprovalStage stage;

    @Column(name = "assigned_to", nullable = false)
    private String assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalStatus status;

    @Lob
    @Column(name = "comment")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bid_id", nullable = false)
    private Bid bid;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = ApprovalStatus.PENDING;
        }
    }

    public void approve(String comment) {
        this.status = ApprovalStatus.APPROVED;
        this.comment = comment;
        this.completedAt = LocalDateTime.now();
    }

    public void reject(String comment) {
        this.status = ApprovalStatus.REJECTED;
        this.comment = comment;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return ApprovalStatus.PENDING.equals(this.status);
    }
}