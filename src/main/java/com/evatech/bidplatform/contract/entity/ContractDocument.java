package com.evatech.bidplatform.contract.entity;

import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.contract.entity.analysis.ContractAnalysisSummary;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlight;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract_document")
public class ContractDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContractStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false)
    private ContractAssignmentStatus assignmentStatus;

    /**
     * Current owner of the contract.
     */
    @Column(name = "assigned_to")
    private String assignedTo;

    /**
     * User who performed the latest assignment/reassignment.
     */
    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "contract_value", precision = 19, scale = 2)
    private BigDecimal contractValue;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "submission_deadline")
    private LocalDate submissionDeadline;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "estimated_bid_count")
    private Integer estimatedBidCount;

    @OneToMany(mappedBy = "contractDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractAssignmentHistory> assignmentHistory =
            new ArrayList<>();

    @OneToMany(mappedBy = "contractDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractLot> lots = new ArrayList<>();

    @OneToMany(mappedBy = "contractDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractPageText> pages = new ArrayList<>();

    @OneToMany(mappedBy = "contractDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractHighlight> highlights = new ArrayList<>();

    @OneToOne(mappedBy = "contractDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContractAnalysisSummary analysisSummary;

    @OneToMany(mappedBy = "contractDocument", fetch = FetchType.LAZY)
    private List<Bid> bids = new ArrayList<>();

    @OneToOne(mappedBy = "contractDocument")
    private Proposal proposal;

    @PrePersist
    public void prePersist() {

        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = ContractStatus.UPLOADED;
        }

        if (this.assignmentStatus == null) {
            this.assignmentStatus = ContractAssignmentStatus.UNASSIGNED;
        }
    }

    /**
     * Initial assignment or reassignment.
     */
    public void assignTo(String assignee, String assignedBy) {
        this.assignedTo = assignee;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now();
        this.assignmentStatus = ContractAssignmentStatus.ASSIGNED;
    }

    /**
     * Used when owner changes.
     */
    public void reassignTo(String newAssignee, String assignedBy) {
        this.assignedTo = newAssignee;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now();
        this.assignmentStatus = ContractAssignmentStatus.ASSIGNED;
    }

    public void unassign(String assignedBy) {
        this.assignedTo = null;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now();
        this.assignmentStatus = ContractAssignmentStatus.UNASSIGNED;
    }

    public boolean isAssigned() {
        return this.assignmentStatus == ContractAssignmentStatus.ASSIGNED;
    }
}