package com.evatech.bidplatform.audit.service;

import com.evatech.bidplatform.audit.dto.AuditResult;
import com.evatech.bidplatform.audit.helper.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    @Around("@annotation(auditAction)")
    public Object audit(
            ProceedingJoinPoint joinPoint,
            AuditAction auditAction) throws Throwable {

        String username = securityUtils.getCurrentUsername();

        String methodName =
                joinPoint.getSignature().getName();

        Long entityId =
                extractEntityId(joinPoint.getArgs());

        try {

            log.info(
                    "Executing action={} entity={} entityId={} user={}",
                    auditAction.action(),
                    auditAction.entity(),
                    entityId,
                    username
            );

            Object result = joinPoint.proceed();

            auditService.save(
                    auditAction.action(),
                    auditAction.entity(),
                    entityId,
                    username,
                    AuditResult.SUCCESS,
                    methodName + " executed successfully"
            );

            return result;

        } catch (Exception ex) {

            auditService.save(
                    auditAction.action(),
                    auditAction.entity(),
                    entityId,
                    username,
                    AuditResult.FAILED,
                    ex.getMessage()
            );

            throw ex;
        }
    }

    private Long extractEntityId(Object[] args) {

        return Arrays.stream(args)
                .filter(Long.class::isInstance)
                .map(Long.class::cast)
                .findFirst()
                .orElse(null);
    }
}