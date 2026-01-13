package com.example.medhub;

import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.entity.AuditLogEntity;
import com.example.medhub.enums.AuditAction;
import com.example.medhub.repository.AuditLogRepository;
import com.example.medhub.service.WorkersService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
class AuditLogIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorkersService workersService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldCreateAuditLogWhenWorkerRegistered() {
        WorkerCreateRequestDTO request = new WorkerCreateRequestDTO();
        request.setEmail("audit.test@example.com");
        request.setPassword("SecurePass123!");
        request.setLocationName("Krakow");
        workersService.saveWorker(request);
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            List<AuditLogEntity> auditLogs = auditLogRepository.findAll();
            assertThat(auditLogs).isNotEmpty();

            AuditLogEntity log = auditLogs.get(auditLogs.size() - 1);
            assertThat(log.getAction()).isEqualTo(AuditAction.WORKER_REGISTERED);
            assertThat(log.getActorEmail()).isIn("SYSTEM", "anonymousUser");
            assertThat(log.getMetadata()).contains("audit.test@example.com");
            assertThat(log.getRequestId()).isNotNull();
        });
    }
}
