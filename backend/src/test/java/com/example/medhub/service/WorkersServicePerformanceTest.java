package com.example.medhub.service;

import com.example.medhub.AbstractIntegrationTest;
import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.enums.AppointmentType;
import com.example.medhub.enums.Authority;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.Worker;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import com.example.medhub.repository.UserRepository;
import com.example.medhub.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkersServicePerformanceTest extends AbstractIntegrationTest {

    @Autowired
    private WorkersService workersService;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private SpecializationRepository specializationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentsRepository appointmentsRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private LocationEntity testLocation;

    @BeforeEach
    void setUp() {
        testLocation = new LocationEntity();
        testLocation.setLocationName("Test Clinic");
        testLocation.setCity("Test City");
        testLocation.setAddress("Test St 1");
        testLocation.setCountry("PL");
        testLocation = locationRepository.save(testLocation);

        Worker worker = new Worker();
        worker.setEmail("worker@test.com");
        worker.setPassword(passwordEncoder.encode("pass"));
        worker.setName("Worker");
        worker.setSurname("One");
        worker.setPhoneNumber("111");
        worker.setAuthority(Authority.ROLE_WORKER);
        worker.setLocation(testLocation);
        workerRepository.save(worker);

        SpecializationEntity spec = new SpecializationEntity();
        spec.setSpecializationName("TestSpec");
        spec = specializationRepository.save(spec);

        Doctor doctor = new Doctor();
        doctor.setName("Doc");
        doctor.setSurname("Tor");
        doctor.setPwz("9999999");
        doctor.setEmail("doc@test.com");
        doctor.setPassword("pass");
        doctor.setPhoneNumber("999");
        doctor.setAuthority(Authority.ROLE_DOCTOR);
        doctor.setSpecialization(spec);
        doctor = doctorRepository.save(doctor);

        Patient user = new Patient();
        user.setEmail("patient@test.com");
        user.setPassword("pass");
        user.setName("Patient");
        user.setSurname("Zero");
        user.setPhoneNumber("222");
        user.setPesel("12345678901");
        user.setAuthority(Authority.ROLE_PATIENT);
        userRepository.save(user);

        for (int i = 0; i < 5; i++) {
            AppointmentsEntity appt = new AppointmentsEntity();
            appt.setLocation(testLocation);
            appt.setDoctor(doctor);
            appt.setPatient(user);
            appt.setDate(LocalDate.now().plusDays(i));
            appt.setTime(LocalTime.of(10, 0));
            appt.setAppointmentStatus(AppointmentStatus.ACTIVE);
            appt.setAppointmentType(AppointmentType.PRIVATE);
            appointmentsRepository.save(appt);
        }

        for (int i = 0; i < 5; i++) {
            AppointmentsEntity appt = new AppointmentsEntity();
            appt.setLocation(testLocation);
            appt.setDoctor(doctor);
            appt.setPatient(null);
            appt.setDate(LocalDate.now().plusWeeks(1).plusDays(i));
            appt.setTime(LocalTime.of(12, 0));
            appt.setAppointmentStatus(AppointmentStatus.ACTIVE);
            appt.setAppointmentType(AppointmentType.NFZ);
            appointmentsRepository.save(appt);
        }
    }

    @Test
    @Transactional
    @WithMockUser(username = "worker@test.com", roles = { "WORKER" })
    void getAppointmentsForCurrentWorker_ShouldBeEfficient() {
        entityManager.flush();
        entityManager.clear();

        List<AppointmentsDto> result = workersService.getAppointmentsForCurrentWorker();

        assertThat(result).hasSize(5);
    }
}
