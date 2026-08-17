package com.evatech.bidplatform.audit.service;

import com.evatech.bidplatform.audit.dto.AuditResult;
import com.evatech.bidplatform.audit.entity.AuditLog;
import com.evatech.bidplatform.audit.repository.AuditLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void save(
            String action,
            String entity,
            Long entityId,
            String username,
            AuditResult result,
            String message) {

        AuditLog auditLog = new AuditLog();

        auditLog.setAction(action);
        auditLog.setEntity(entity);
        auditLog.setEntityId(entityId);
        auditLog.setUsername(username);
        auditLog.setResult(result);
        auditLog.setMessage(message);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }
}