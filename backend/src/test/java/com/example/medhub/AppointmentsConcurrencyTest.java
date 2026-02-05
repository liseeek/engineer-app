package com.example.medhub;

import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.enums.AppointmentType;
import com.example.medhub.enums.Authority;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import com.example.medhub.repository.UserRepository;
import com.example.medhub.service.AppointmentsService;
import com.example.medhub.entity.SpecializationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AppointmentsConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private AppointmentsRepository appointmentsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    private Long testAppointmentId;
    private Patient testUser1;
    private Patient testUser2;

    @BeforeEach
    void setUp() {
        appointmentsRepository.deleteAll();
        userRepository.deleteAll();

        LocationEntity location = locationRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    LocationEntity loc = new LocationEntity();
                    loc.setLocationName("Test Clinic");
                    loc.setCity("Warsaw");
                    return locationRepository.save(loc);
                });

        SpecializationEntity specialization = specializationRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    SpecializationEntity spec = new SpecializationEntity();
                    spec.setSpecializationName("Internal Medicine");
                    return specializationRepository.save(spec);
                });

        Doctor doctor = doctorRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Doctor doc = new Doctor();
                    doc.setName("Dr. Test");
                    doc.setSurname("Doctor");
                    doc.setPwz("1234567");
                    doc.setEmail("doctor@test.com");
                    doc.setPassword("pass");
                    doc.setPhoneNumber("123456789");
                    doc.setAuthority(Authority.ROLE_DOCTOR);
                    doc.setSpecialization(specialization);
                    return doctorRepository.save(doc);
                });

        testUser1 = new Patient();
        testUser1.setName("Jan");
        testUser1.setSurname("Kowalski");
        testUser1.setEmail("user1@test.com");
        testUser1.setPassword("password123");
        testUser1.setPhoneNumber("111222333");
        testUser1.setAuthority(Authority.ROLE_PATIENT);
        testUser1 = userRepository.save(testUser1);

        testUser2 = new Patient();
        testUser2.setName("Anna");
        testUser2.setSurname("Nowak");
        testUser2.setEmail("user2@test.com");
        testUser2.setPassword("password123");
        testUser2.setPhoneNumber("444555666");
        testUser2.setAuthority(Authority.ROLE_PATIENT);
        testUser2 = userRepository.save(testUser2);

        AppointmentsEntity appointment = new AppointmentsEntity();
        appointment.setLocation(location);
        appointment.setDoctor(doctor);
        appointment.setDate(LocalDate.now().plusDays(1));
        appointment.setTime(LocalTime.of(10, 0));
        appointment.setAppointmentStatus(AppointmentStatus.ACTIVE);
        appointment.setAppointmentType(AppointmentType.NFZ);
        appointment.setPatient(null);

        testAppointmentId = appointmentsRepository.save(appointment).getAppointmentId();
    }

    @Test
    @DisplayName("Only one user can book the same appointment slot (Concurrency Safety)")
    void shouldPreventDoubleBooking() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Runnable bookingTask1 = () -> {
            try {
                startLatch.await();
                setSecurityContext(testUser1);
                appointmentsService.addAppointmentToUser(testAppointmentId);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                SecurityContextHolder.clearContext();
                doneLatch.countDown();
            }
        };

        Runnable bookingTask2 = () -> {
            try {
                startLatch.await();
                setSecurityContext(testUser2);
                appointmentsService.addAppointmentToUser(testAppointmentId);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                SecurityContextHolder.clearContext();
                doneLatch.countDown();
            }
        };

        executor.submit(bookingTask1);
        executor.submit(bookingTask2);

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(successCount.get())
                .as("Exactly one booking should succeed")
                .isEqualTo(1);
        assertThat(failureCount.get())
                .as("Exactly one booking should fail due to concurrency control")
                .isEqualTo(1);

        AppointmentsEntity bookedAppointment = appointmentsRepository.findById(testAppointmentId).orElseThrow();
        assertThat(bookedAppointment.getPatient()).isNotNull();
    }

    private void setSecurityContext(Patient user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
