package com.example.medhub.event;

import com.example.medhub.enums.AuditAction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class WorkerRegisteredEvent extends ApplicationEvent {
        private final String actorEmail;
        private final String workerEmail;
        private final String resourceId;
        private final AuditAction action;
        private final Instant eventTime;

        public WorkerRegisteredEvent(Object source, String actorEmail, String workerEmail, String resourceId) {
                super(source);
                this.actorEmail = actorEmail;
                this.workerEmail = workerEmail;
                this.resourceId = resourceId;
                this.action = AuditAction.WORKER_REGISTERED;
                this.eventTime = Instant.now();
        }
}
