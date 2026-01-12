package com.example.medhub;

import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.enums.AuditAction;
import com.example.medhub.entity.AuditLogEntity;
import com.example.medhub.repository.AuditLogRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.WorkerRepository;
import com.example.medhub.service.WorkersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AuditLogIntegrationTest extends AbstractIntegrationTest {

    @Autowired private WorkersService workersService;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private WorkerRepository workerRepository;
    @Autowired private LocationRepository locationRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAllInBatch();
        workerRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
    }

    @Test
    @WithMockUser(username = "audit-admin@medhub.pl")
    void shouldLogWorkerRegistrationInAuditTable() {
        String workerEmail = "worker@medhub.pl";
        LocationEntity location = new LocationEntity();
        location.setLocationName("MaxMed");
        location.setAddress("Kolejowa");
        location.setCity("Kazimierza");
        location.setCountry("Poland");
        locationRepository.save(location);

        WorkerCreateRequestDTO worker = new WorkerCreateRequestDTO();
        worker.setName("Gregory");
        worker.setSurname("House");
        worker.setEmail(workerEmail);
        worker.setPassword("secretPassword");
        worker.setPasswordConfirmation("secretPassword");
        worker.setPhoneNumber("123456789");
        worker.setLocationName("MaxMed");

        workersService.saveWorker(worker);

        await().atMost(5, SECONDS).until(() -> auditLogRepository.count() > 0);

        List<AuditLogEntity> logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        AuditLogEntity logEntry = logs.get(0);

        assertThat(logEntry.getUserEmail()).isEqualTo("audit-admin@medhub.pl");
        assertThat(logEntry.getAction()).isEqualTo(AuditAction.WORKER_REGISTERED);
        assertThat(logEntry.getDetails()).contains(workerEmail);
    }
}
