package com.evatech.bidplatform.audit.entity;

import com.evatech.bidplatform.audit.dto.AuditResult;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    private String entity;

    private Long entityId;

    private String username;

    @Enumerated(EnumType.STRING)
    private AuditResult result;

    @Column(length = 4000)
    private String message;

    private LocalDateTime createdAt;
}