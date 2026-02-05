package com.example.medhub.listener;

import com.example.medhub.entity.AuditLogEntity;
import com.example.medhub.event.WorkerRegisteredEvent;
import com.example.medhub.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @Async
    @EventListener
    public void handleWorkerRegistered(WorkerRegisteredEvent event) {
        log.debug("Processing audit event for worker registration: {}", event.getWorkerEmail());

        String requestId = MDC.get("requestId");

        AuditLogEntity auditLog = AuditLogEntity.builder()
                .timestamp(java.time.LocalDateTime.ofInstant(event.getEventTime(), java.time.ZoneId.systemDefault()))
                .actorEmail(event.getActorEmail())
                .action(event.getAction().name())
                .resourceId(event.getResourceId())
                .requestId(requestId != null ? requestId : "ASYNC_NO_ID")
                .metadata("Worker registered: " + event.getWorkerEmail())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log saved: [Action: {}, Resource: {}, RequestId: {}]",
                auditLog.getAction(), auditLog.getResourceId(), requestId);
    }
}
