package com.evatech.bidplatform.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_assignment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractDocument contractDocument;

    @Column(name = "old_assignee")
    private String oldAssignee;

    @Column(name = "new_assignee", nullable = false)
    private String newAssignee;

    @Column(name = "assigned_by", nullable = false)
    private String assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "remarks")
    private String remarks;
}