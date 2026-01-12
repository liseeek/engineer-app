package com.example.medhub.listener;

import com.example.medhub.entity.AuditLogEntity;
import com.example.medhub.enums.AuditAction;
import com.example.medhub.event.WorkerRegisteredEvent;
import com.example.medhub.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWorkerRegistered(WorkerRegisteredEvent event) {
        try {
            String detailsJson = objectMapper.writeValueAsString(event);
            
            AuditLogEntity logEntry = AuditLogEntity.builder()
                    .userEmail(event.performedBy() != null ? event.performedBy() : "SYSTEM")
                    .action(AuditAction.WORKER_REGISTERED)
                    .resourceId(event.workerId())
                    .details(detailsJson)
                    .build();

            auditLogRepository.save(logEntry);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event details", e);
        }
    }
}
