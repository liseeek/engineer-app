package com.example.medhub.service;

import com.example.medhub.entity.AuditLogEntity;
import com.example.medhub.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actorEmail, String action, String resourceId, String requestId, String metadata) {
        log.info("AUDIT: Actor={}, Action={}, Resource={}", actorEmail, action, resourceId);

        AuditLogEntity auditLog = AuditLogEntity.builder()
                .actorEmail(actorEmail)
                .action(action)
                .resourceId(resourceId)
                .requestId(requestId)
                .metadata(metadata)
                .build();

        auditLogRepository.save(auditLog);
    }
}
