package com.example.medhub.seeder;

import com.example.medhub.entity.Admin;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.Authority;
import com.example.medhub.enums.DoctorVerificationStatus;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.PatientRepository;
import com.example.medhub.repository.SpecializationRepository;
import com.example.medhub.repository.UserRepository;
import com.example.medhub.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final WorkerRepository workerRepository;
    private final PatientRepository patientRepository;
    private final LocationRepository locationRepository;
    private final SpecializationRepository specializationRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${database.admin.name}")
    private String adminName;

    @Value("${database.admin.surname}")
    private String adminSurname;

    @Value("${database.admin.email}")
    private String adminEmail;

    @Value("${database.admin.phoneNumber}")
    private String adminPhoneNumber;

    @Value("${database.admin.password}")
    private String adminPassword;

    @Value("${database.demo.password}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();

        if (Arrays.asList(environment.getActiveProfiles()).contains("local")) {
            seedDemoData();
        } else {
            log.info("Profile 'local' not active. Skipping demo data seeding.");
        }
    }

    private void seedAdmin() {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("Seeding Admin account...");
            Admin admin = new Admin();
            admin.setName(adminName);
            admin.setSurname(adminSurname);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setPhoneNumber(adminPhoneNumber);
            admin.setAuthority(Authority.ROLE_ADMIN);
            userRepository.save(admin);
            log.info("Admin account created.");
        } else {
            log.info("Admin account already exists.");
        }
    }

    private void seedDemoData() {
        if (userRepository.findByEmail("john.doe@medhub.pl").isPresent()) {
            log.info("Demo data already present — skipping demo seeder.");
            return;
        }

        log.info("Seeding demo data…");
        String encoded = passwordEncoder.encode(demoPassword);

        seedDoctors(encoded);
        seedWorker(encoded);
        seedPatient(encoded);

        log.info("Demo data seeding complete.");
    }

    private void seedDoctors(String encodedPassword) {
        LocationEntity warsaw = findLocation("City Health Clinic");
        LocationEntity krakow = findLocation("Suburban Medical Center");
        LocationEntity gdansk = findLocation("Lakeside Hospital");
        LocationEntity zakopane = findLocation("Mountain Medical Facility");

        createDoctor("John", "Doe", "john.doe@medhub.pl", "1234567",
                encodedPassword, List.of(warsaw, krakow),
                List.of(findSpec("Cardiology")));

        createDoctor("Anna", "Smith", "anna.smith@medhub.pl", "2345678",
                encodedPassword, List.of(warsaw),
                List.of(findSpec("Dermatology and Venereology")));

        createDoctor("Michael", "Brown", "michael.brown@medhub.pl", "3456789",
                encodedPassword, List.of(krakow, gdansk),
                List.of(findSpec("Orthopedics and Traumatology")));

        createDoctor("Sarah", "Johnson", "sarah.johnson@medhub.pl", "4567890",
                encodedPassword, List.of(gdansk),
                List.of(findSpec("Pediatrics")));

        createDoctor("Robert", "Wilson", "robert.wilson@medhub.pl", "5678901",
                encodedPassword, List.of(zakopane),
                List.of(findSpec("Psychiatry")));
    }

    private void createDoctor(String name, String surname, String email, String pwz,
            String encodedPassword, List<LocationEntity> locations,
            List<SpecializationEntity> specializations) {
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setSurname(surname);
        doctor.setEmail(email);
        doctor.setPassword(encodedPassword);
        doctor.setPhoneNumber("123456789");
        doctor.setAuthority(Authority.ROLE_DOCTOR);
        doctor.setPwz(pwz);
        doctor.setVerificationStatus(DoctorVerificationStatus.VERIFIED);
        doctor.getLocations().addAll(locations);
        doctor.getSpecializations().addAll(specializations);
        doctorRepository.save(doctor);
        log.debug("Created demo doctor: {}", email);
    }

    private void seedWorker(String encodedPassword) {
        Worker worker = new Worker();
        worker.setName("Emily");
        worker.setSurname("Clark");
        worker.setEmail("emily.clark@medhub.pl");
        worker.setPassword(encodedPassword);
        worker.setPhoneNumber("111222333");
        worker.setAuthority(Authority.ROLE_WORKER);
        worker.setLocation(findLocation("City Health Clinic"));
        workerRepository.save(worker);
        log.debug("Created demo worker: emily.clark@medhub.pl");
    }

    private void seedPatient(String encodedPassword) {
        Patient patient = new Patient();
        patient.setName("James");
        patient.setSurname("Miller");
        patient.setEmail("james.miller@medhub.pl");
        patient.setPassword(encodedPassword);
        patient.setPhoneNumber("444555666");
        patient.setAuthority(Authority.ROLE_PATIENT);
        patient.setPesel("92010112345");
        patientRepository.save(patient);
        log.debug("Created demo patient: james.miller@medhub.pl");
    }

    private LocationEntity findLocation(String name) {
        return locationRepository.findLocationByLocationName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Location '" + name + "' not found — ensure Liquibase migration 10 has run."));
    }

    private SpecializationEntity findSpec(String name) {
        return specializationRepository.findSpecializationEntityBySpecializationName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "Specialization '" + name + "' not found — ensure Liquibase migration 07 has run."));
    }
}
