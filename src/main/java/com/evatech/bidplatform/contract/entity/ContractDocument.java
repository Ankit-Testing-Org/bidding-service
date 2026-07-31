package com.evatech.bidplatform.contract.entity;

import com.evatech.bidplatform.bid.entity.Bid;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
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

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "contractDocument",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ContractPageText> pages = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "contractDocument",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ContractHighlight> highlights = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "contractDocument", fetch = FetchType.LAZY)
    private List<Bid> bids = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = ContractStatus.UPLOADED;
        }

        if (this.assignmentStatus == null) {
            this.assignmentStatus = ContractAssignmentStatus.UNASSIGNED;
        }
    }

    public void assignTo(String assignedTo) {
        this.assignedTo = assignedTo;
        this.assignedAt = LocalDateTime.now();
        this.assignmentStatus = ContractAssignmentStatus.ASSIGNED;
    }

    public void unassign() {
        this.assignedTo = null;
        this.assignedAt = null;
        this.assignmentStatus = ContractAssignmentStatus.UNASSIGNED;
    }
}