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
@Table(name = "approval_history")
public class ApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_by", nullable = false)
    private String actionBy;

    @Column(name = "action", nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private ApprovalStage stage;

    @Lob
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bid_id", nullable = false)
    private Bid bid;

    @PrePersist
    public void prePersist() {
        this.actionAt = LocalDateTime.now();
    }
}