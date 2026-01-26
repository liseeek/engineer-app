package com.example.medhub;

import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Worker;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.enums.AppointmentType;
import com.example.medhub.enums.Authority;
import com.example.medhub.repository.AuditLogRepository;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.WorkerRepository;
import com.example.medhub.repository.SpecializationRepository;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.service.FacilityOperationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuditLogIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FacilityOperationService facilityOperationService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AppointmentsRepository appointmentsRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Test
    @Transactional
    @WithMockUser(username = "worker@test.com", roles = "WORKER")
    void shouldLogCancellationAudit() {

        LocationEntity location = new LocationEntity();
        location.setCity("Test City");
        location.setAddress("Test St");
        location.setLocationName("Test Clinic");
        location.setCountry("Poland");
        locationRepository.save(location);

        SpecializationEntity spec = new SpecializationEntity();
        spec.setSpecializationName("AuditSpec_" + System.currentTimeMillis());
        specializationRepository.save(spec);

        Doctor doctor = new Doctor();
        doctor.setEmail("doc@audit.com");
        doctor.setName("Doc");
        doctor.setSurname("Audit");
        doctor.setPassword("pass");
        doctor.setPhoneNumber("123");
        doctor.setAuthority(Authority.ROLE_DOCTOR);
        doctor.setPwz("7777777");
        doctor.setSpecialization(spec);
        doctorRepository.save(doctor);

        var worker = new Worker();
        worker.setEmail("worker@test.com");
        worker.setPassword("pass");
        worker.setName("Work");
        worker.setSurname("Audit");
        worker.setPhoneNumber("456");
        worker.setAuthority(Authority.ROLE_WORKER);
        worker.setLocation(location);
        workerRepository.save(worker);

        AppointmentsEntity appointment = new AppointmentsEntity();
        appointment.setDoctor(doctor);
        appointment.setLocation(location);
        appointment.setDate(LocalDate.now());
        appointment.setTime(LocalTime.now());
        appointment.setAppointmentStatus(AppointmentStatus.ACTIVE);
        appointment.setAppointmentType(AppointmentType.NFZ);
        appointmentsRepository.save(appointment);

        facilityOperationService.cancelAppointment(appointment.getAppointmentId(), "Emergency");

        long count = auditLogRepository.count();
        assertThat(count).isGreaterThan(0);

        var logs = auditLogRepository.findAll();
        assertThat(logs).anyMatch(log -> log.getAction().equals("CANCEL_APPOINTMENT") &&
                log.getResourceId().equals(appointment.getAppointmentId().toString()) &&
                log.getActorEmail().equals("worker@test.com"));
    }
}
